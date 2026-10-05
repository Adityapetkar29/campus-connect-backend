package com.campusconnect.backend.controller;

import com.campusconnect.backend.entity.Book;
import com.campusconnect.backend.entity.IssuedBook;
import com.campusconnect.backend.repository.BookRepository;
import com.campusconnect.backend.repository.IssuedBookRepository;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
public class LibraryWebController {

    private final BookRepository bookRepository;
    private final IssuedBookRepository issuedBookRepository;

    public LibraryWebController(
            BookRepository bookRepository,
            IssuedBookRepository issuedBookRepository) {

        this.bookRepository = bookRepository;
        this.issuedBookRepository = issuedBookRepository;
    }

    @GetMapping("/library/login")
    public String libraryLogin() {
        return "library/login";
    }

    @GetMapping("/library")
    public String libraryDashboard(Model model) {

        var books = bookRepository.findAll();

        int totalBooks = books.stream()
                .mapToInt(Book::getTotalCopies)
                .sum();

        int availableBooks = books.stream()
                .mapToInt(Book::getAvailableCopies)
                .sum();

        int issuedBooks = totalBooks - availableBooks;

        model.addAttribute("totalBooks", totalBooks);
        model.addAttribute("availableBooks", availableBooks);
        model.addAttribute("issuedBooks", issuedBooks);

        long overdueBooks = issuedBookRepository.findAll().stream()
                .filter(issued -> issued.getReturnDate() == null)
                .filter(issued -> issued.getDueDate() != null)
                .filter(issued -> issued.getDueDate().isBefore(LocalDate.now()))
                .count();

        model.addAttribute("overdueBooks", overdueBooks);

        return "library/dashboard";
    }

    @GetMapping("/library/books")
    public String books(
            @RequestParam(required = false) String search,
            Model model) {

        if (search != null && !search.trim().isEmpty()) {

            var titleBooks =
                    bookRepository.findByTitleContainingIgnoreCase(search);

            var authorBooks =
                    bookRepository.findByAuthorContainingIgnoreCase(search);

            titleBooks.addAll(authorBooks);

            model.addAttribute("books", titleBooks);

        } else {
            model.addAttribute("books", bookRepository.findAll());
        }

        model.addAttribute("search", search);

        return "library/books";
    }

    @GetMapping("/library/books/add")
    public String addBook() {
        return "library/books/add-book";
    }

    // ISSUE BOOK PAGE
    @GetMapping("/library/books/issue")
    public String issueBookPage(Model model) {
        model.addAttribute("books", bookRepository.findAll());
        return "library/books/issue-book";
    }

    // ISSUE BOOK
    @PostMapping("/library/books/issue")
    public String issueBook(
            @RequestParam String studentName,
            @RequestParam Long bookId) {

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        if (book.getAvailableCopies() <= 0) {
            return "redirect:/library/books/issue?error=unavailable";
        }

        IssuedBook issuedBook = new IssuedBook();

        issuedBook.setStudentName(studentName);
        issuedBook.setIssueDate(LocalDate.now());
        issuedBook.setBook(book);

        issuedBookRepository.save(issuedBook);

        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);

        return "redirect:/library/issued";
    }

    // ISSUED BOOKS
    @GetMapping("/library/issued")
    public String issuedBooks(Model model) {

        model.addAttribute(
                "issuedBooks",
                issuedBookRepository.findAll()
        );

        return "library/books/issued-books";
    }

    // RETURN BOOK
    @PostMapping("/library/books/return/{id}")
    public String returnBook(@PathVariable Long id) {

        IssuedBook issuedBook = issuedBookRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Issued book not found"));

        if (issuedBook.getReturnDate() == null) {

            issuedBook.setReturnDate(LocalDate.now());

            Book book = issuedBook.getBook();

            book.setAvailableCopies(
                    book.getAvailableCopies() + 1
            );

            bookRepository.save(book);
            issuedBookRepository.save(issuedBook);
        }

        return "redirect:/library/issued";
    }

    // DELETE BOOK
    @Transactional
    @PostMapping("/library/books/delete/{id}")
    public String deleteBook(@PathVariable Long id) {

        Book book = bookRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Book not found"));

        // Check if book is currently issued
        boolean currentlyIssued = issuedBookRepository.findAll()
                .stream()
                .anyMatch(issued ->
                        issued.getBook().getBookId().equals(id)
                                && issued.getReturnDate() == null
                );

        // Do not delete if currently issued
        if (currentlyIssued) {
            return "redirect:/library/books?error=issued";
        }

        // Delete old issue history of this book
        var issuedRecords = issuedBookRepository.findAll()
                .stream()
                .filter(issued ->
                        issued.getBook().getBookId().equals(id)
                )
                .toList();

        issuedBookRepository.deleteAll(issuedRecords);

        // Delete the book
        bookRepository.delete(book);

        return "redirect:/library/books";
    }

    // DELETE INDIVIDUAL ISSUED BOOK HISTORY
    @Transactional
    @PostMapping("/library/issued/delete/{id}")
    public String deleteIssuedBookHistory(
            @PathVariable Long id) {

        IssuedBook issuedBook = issuedBookRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Issued book history not found"));

        // Only delete returned/history records.
        // Currently issued books cannot be deleted from history.
        if (issuedBook.getReturnDate() == null) {
            return "redirect:/library/issued?error=active";
        }

        issuedBookRepository.delete(issuedBook);

        return "redirect:/library/issued?deleted=true";
    }

    // OVERDUE BOOKS
    @GetMapping("/library/overdue")
    public String overdueBooks(Model model) {

        LocalDate today = LocalDate.now();

        var overdueBooks = issuedBookRepository.findAll().stream()
                .filter(issued -> issued.getReturnDate() == null)
                .filter(issued -> issued.getDueDate() != null)
                .filter(issued ->
                        issued.getDueDate().isBefore(today)
                )
                .toList();

        model.addAttribute("overdueBooks", overdueBooks);

        return "library/books/overdue-books";
    }
}