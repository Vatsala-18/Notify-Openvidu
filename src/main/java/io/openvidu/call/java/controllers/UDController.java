package io.openvidu.call.java.controllers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class UDController {
    private static final String FILE_DIRECTORY = "/home/Vatsala.Vats/vid-content/";

    @PostMapping("/upload")
    public ResponseEntity<String> handleFileUpload(@RequestParam(value = "file", required = false) MultipartFile file) throws IOException {
        System.out.println(file);
        try{
            if(file.isEmpty()){
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Request must contain file");
            }

         }
        catch(Exception e){

        }

        Files.copy(file.getInputStream(), Paths.get(FILE_DIRECTORY,file.getOriginalFilename()), StandardCopyOption.REPLACE_EXISTING);
        return ResponseEntity.ok("File Uploaded Successfully");
    }

    @GetMapping("/download/{filename}")
    public ResponseEntity<byte[]> handleFileDownload(@PathVariable("filename") String filename) throws IOException {
        Path file = Paths.get(FILE_DIRECTORY, filename);
        byte[] bytes = Files.readAllBytes(file);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
        headers.setContentLength(bytes.length);
        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }
}
