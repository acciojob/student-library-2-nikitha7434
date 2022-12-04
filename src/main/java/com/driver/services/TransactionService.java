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
import sun.util.resources.LocaleData;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
        //check whether bookId and cardId already exist
        //conditions required for successful transaction of issue book:
        //1. book is present and available
        // If it fails: throw new Exception("Book is either unavailable or not present");
        //2. card is present and activated
        // If it fails: throw new Exception("Card is invalid");
        //3. number of books issued against the card is strictly less than max_allowed_books
        // If it fails: throw new Exception("Book limit has reached for this card");
        //If the transaction is successful, save the transaction to the list of transactions and return the id


        //Note that the error message should match exactly in all cases
        Transaction transaction=null;

        Card card =cardRepository5.findById(cardId).get();
        Book book=bookRepository5.findById(bookId).get();


        if (book == null){
            throw new Exception("Book is either unavailable or not present");
        }else{
           transaction.setBook(book);
        }

        if(card== null || card.getCardStatus().equals("DEACTIVATED")){
            throw new Exception("Card is invalid");
        }else{
            transaction.setCard(card);
        }

        SimpleDateFormat df =new SimpleDateFormat("yyyy-mm-dd");
        String today =df.format(new Date());

        List<Transaction> transactionList =transactionRepository5.findAll();
        int allocated_book_card=0;
        int total_issue_book_today=0;

        for(Transaction  transaction1 :transactionList){

            if(transaction1.isIssueOperation()  &&
                    transaction1.getCard().equals(card) && transaction1.getTransactionStatus().equals("SUCCESSFUL")){
                allocated_book_card++;
            }

            if(transaction1.isIssueOperation() && transaction1.getTransactionDate().equals(today)){
                total_issue_book_today++;
            }
        }

        if (allocated_book_card>=max_allowed_books || total_issue_book_today>=getMax_allowed_days){
            throw new Exception("Book limit has reached for this card");
        }else{
            transaction.setTransactionStatus(TransactionStatus.SUCCESSFUL);
            transaction.setTransactionDate(new Date());
            transaction.setIssueOperation(true);
        }

        transactionRepository5.save(transaction);
        bookRepository5.updateBook(book);

       return transaction.getTransactionId(); //return transactionId instead
    }

    public Transaction returnBook(int cardId, int bookId) throws Exception{

        List<Transaction> transactions = transactionRepository5.find(cardId, bookId, TransactionStatus.SUCCESSFUL, true);
        Transaction transaction = transactions.get(transactions.size() - 1);

        Card card =cardRepository5.findById(cardId).get();
        Book book=bookRepository5.findById(bookId).get();

        String issuedate ="";
        SimpleDateFormat df =new SimpleDateFormat("yyyy-mm-dd");
        String retunday =df.format(new Date());

        for(Transaction transaction1:transactions){

            if (transaction.isIssueOperation() && transaction1.getCard().equals(card)){
                issuedate= String.valueOf(transaction1.getTransactionDate());
            }
        }



        //for the given transaction calculate the fine amount considering the book has been returned exactly when this function is called
        //make the book available for other users
        //make a new transaction for return book which contains the fine amount as well

        Transaction returnBookTransaction  = Transaction.builder()
                .card(card).book(book).fineAmount(transaction.getFineAmount()).build();
        bookRepository5.updateBook(book);
        return returnBookTransaction; //return the transaction after updating all details
    }
}
