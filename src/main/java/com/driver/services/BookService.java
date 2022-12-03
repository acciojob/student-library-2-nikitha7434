package com.driver.services;

import com.driver.models.Book;
import com.driver.models.Genre;
import com.driver.repositories.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {


    @Autowired
    BookRepository bookRepository2;

    public void createBook(Book book){


        bookRepository2.save(book);
    }

    public List<Book> getBooks(String genre, boolean available, String author){
        List<Book> books = null; //find the elements of the list by yourself

if(avilableGener(genre) && available == true) {
    books = bookRepository2.findBooksByGenre(genre, available);
}
else if(avilableGener(genre) && available==false) {
    books = bookRepository2.findBooksByAuthor(author, available);
}else if(author.equals("A")) {
    books = bookRepository2.findBooksByGenreAuthor(genre, author, available);
}
else if(available==true) {
    books = bookRepository2.findByAvailability(available);
}
        return books;
    }

    private boolean avilableGener(String genre){
        if((Genre.BOTANY).equals(genre) ||
                Genre.CHEMISTRY.equals(genre) || Genre.FICTIONAL.equals(genre)
                || Genre.GEOGRAPHY.equals(genre) || Genre.HISTORY.equals(genre) ||
                Genre.MATHEMATICS.equals(genre) || Genre.NON_FICTIONAL.equals(genre) ||
                Genre.POLITICAL_SCIENCE.equals(genre) || Genre.PHYSICS.equals(genre)) {
            return true;
        }
          return false;
    }
}
