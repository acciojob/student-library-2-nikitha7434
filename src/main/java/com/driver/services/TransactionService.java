package com.driver.services;

import com.driver.models.Book;
import com.driver.models.Card;
import com.driver.models.Transaction;
import com.driver.models.TransactionStatus;
import com.driver.repositories.BookRepository;
import com.driver.repositories.CardRepository;
import com.driver.repositories.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class TransactionService {

    @Autowired
    BookRepository bookRepository5;

    @Autowired
    CardRepository cardRepository5;

    @Autowired
    TransactionRepository transactionRepository5;

    @Value("${max_allowed_books}")
    public int max_allowed_books;

    @Value("${books.max_allowed_days}")
    public int getMax_allowed_days;

    @Value("${books.fine.per_day}")
    public int fine_per_day;

    public String issueBook(int cardId, int bookId) throws Exception {

        Card card =cardRepository5.findById(cardId).get();
        Book book=bookRepository5.findById(bookId).get();
        //Note that the error message should match exactly in all cases
        Transaction transaction = new Transaction();


          transaction.setBook(book);
          transaction.setCard(card);

        if (book == null || !book.isAvailable()){
            transaction.setTransactionStatus(TransactionStatus.FAILED);
            transactionRepository5.save(transaction);
            throw new Exception("Book is either unavailable or not present");
        }

        if(card== null || card.getCardStatus().equals("DEACTIVATED")){
            transaction.setTransactionStatus(TransactionStatus.FAILED);
            transactionRepository5.save(transaction);
            throw new Exception("Card is invalid");
        }
        if(card.getBooks().size()>=max_allowed_books){
            transaction.setTransactionStatus(TransactionStatus.FAILED);
            transactionRepository5.save(transaction);
            throw new Exception("Book limit has reached for this card");
        }

          book.setCard(card);
        book.setAvailable(false);
        List<Book> booklist=card.getBooks();
        booklist.add(book);
        card.setBooks(booklist);


        bookRepository5.updateBook(book);
        transaction.setTransactionStatus(TransactionStatus.SUCCESSFUL);
        transactionRepository5.save(transaction);

       return transaction.getTransactionId(); //return transactionId instead
    }

    public Transaction returnBook(int cardId, int bookId) throws Exception {

        List<Transaction> transactions = transactionRepository5.find(cardId, bookId, TransactionStatus.SUCCESSFUL, true);
        Transaction transaction = transactions.get(transactions.size() - 1);



        // current day
        Date issuedate = transaction.getTransactionDate();
        long timeissuse =Math.abs(System.currentTimeMillis()-issuedate.getTime());
        long no_of_days_passed = TimeUnit.DAYS.convert(timeissuse,TimeUnit.MILLISECONDS);

      // book issue date


        int fine =0;
        if(no_of_days_passed>getMax_allowed_days){
            fine =(int)((no_of_days_passed-getMax_allowed_days)*fine_per_day);
        }

Book book =transaction.getBook();
        book.setAvailable(true);
        book.setCard(null);

        bookRepository5.updateBook(book);
        Transaction returnBookTransaction =new Transaction();
        returnBookTransaction.setBook(transaction.getBook());
        returnBookTransaction.setCard(transaction.getCard());
        returnBookTransaction.setIssueOperation(false);
        returnBookTransaction.setFineAmount(fine);
        returnBookTransaction.setTransactionStatus(TransactionStatus.SUCCESSFUL);
        transactionRepository5.save(returnBookTransaction);

        Card card =cardRepository5.findById(cardId).get();

        List<Book> booklist=card.getBooks();
        booklist.remove(book);
        card.setBooks(booklist);




        return returnBookTransaction; //return the transaction after updating all details
    }
}
