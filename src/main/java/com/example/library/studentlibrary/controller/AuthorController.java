package com.example.library.studentlibrary.controller;

import com.example.library.studentlibrary.models.Author;
import com.example.library.studentlibrary.models.Student;
import com.example.library.studentlibrary.services.AuthorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//Add required annotations

@Slf4j
@RestController
@RequestMapping
public class AuthorController {
    @Autowired
    //Write createAuthor API with required annotations
    AuthorService authorService;
    @PostMapping("/author")
    public ResponseEntity<String> addAuthor(@RequestBody() Author author){
        authorService.create(author);
        return new ResponseEntity<>("success", HttpStatus.CREATED);
    }

}
