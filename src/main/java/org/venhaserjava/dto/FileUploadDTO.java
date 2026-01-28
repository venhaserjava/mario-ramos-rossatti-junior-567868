package org.venhaserjava.dto;

import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

public class FileUploadDTO {
    @RestForm("file")
    public FileUpload file;
}