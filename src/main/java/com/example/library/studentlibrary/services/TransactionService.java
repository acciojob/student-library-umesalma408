package com.example.library.studentlibrary.services;

import com.example.library.studentlibrary.models.*;
import com.example.library.studentlibrary.repositories.BookRepository;
import com.example.library.studentlibrary.repositories.CardRepository;
import com.example.library.studentlibrary.repositories.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

    @Value("${books.max_allowed}")
    int max_allowed_books;

    @Value("${books.max_allowed_days}")
    int getMax_allowed_days;

    @Value("${books.fine.per_day}")
    int fine_per_day;

    public String issueBook(int cardId, int bookId) throws Exception {
        //check whether bookId and cardId already exist
        Card card = cardRepository5.findById(cardId).get();
        Book book = bookRepository5.findById(bookId).get();
        //conditions required for successful transaction of issue book:
        //1. book is present and available
        // If it fails: throw new Exception("Book is either unavailable or not present");
        if(!book.isAvailable()){
            throw new Exception("Book is either unavailable or not present");
        }
        //2. card is present and activated
        // If it fails: throw new Exception("Card is invalid");
        if(!(card.getCardStatus() == CardStatus.ACTIVATED)){
            throw new Exception("Card is invalid");
        }
        //3. number of books issued against the card is strictly less than max_allowed_books
        // If it fails: throw new Exception("Book limit has reached for this card");
        if(!(card.getBooks().size()<max_allowed_books)){
            throw new Exception("Book limit has reached for this card");
        }
        //If the transaction is successful, save the transaction to the list of transactions and return the id
            Transaction transaction = new Transaction();
        transaction.setBook(book);
        transaction.setCard(card);
        transaction.setTransactionStatus(TransactionStatus.SUCCESSFUL);
        transaction.setIssueOperation(true);
        transactionRepository5.save(transaction);
        //////////////////////////////////////////////////
        book.setCard(card);
        List<Transaction> newList = book.getTransactions();
        newList.add(transaction);
        book.setTransactions(newList);
        bookRepository5.save(book);
        ///////////////////////////////////
        List<Book> newBooks = card.getBooks();
        newBooks.add(book);
        card.setBooks(newBooks);
        cardRepository5.save(card);
        //Note that the error message should match exactly in all cases

       return transaction.getTransactionId(); //return transactionId instead
    }

    public Transaction returnBook(int cardId, int bookId) throws Exception{

        List<Transaction> transactions = transactionRepository5.find(cardId, bookId,TransactionStatus.SUCCESSFUL, true);
        Transaction transaction = transactions.get(transactions.size() - 1);

        Card card = cardRepository5.findById(cardId).get();
        Book book = bookRepository5.findById(bookId).get();
        //for the given transaction calculate the fine amount considering the book has been returned exactly when this function is called
       int days =0;
       Date issueDate = transaction.getTransactionDate();
       Date currDate = new Date();
       long diff = currDate.getTime()-issueDate.getTime();
       days+=diff/86400000;//86400000millis=1day
        days-=getMax_allowed_days;
        if(days<0){
            days=0;
        }
        transaction.setFineAmount(days*5);
        //make the book available for other users
        book.setAvailable(true);
        bookRepository5.save(book);
        //make a new transaction for return book which contains the fine amount as well

        Transaction returnBookTransaction  = new Transaction();
        returnBookTransaction.setBook(book);
        returnBookTransaction.setCard(card);//return the transaction after updating all details
        returnBookTransaction.setTransactionStatus(TransactionStatus.SUCCESSFUL);
        returnBookTransaction.setIssueOperation(true);
        returnBookTransaction.setFineAmount(days*fine_per_day);
        transactionRepository5.save(returnBookTransaction);
        return returnBookTransaction;
    }
}