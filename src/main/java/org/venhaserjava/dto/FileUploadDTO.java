package org.venhaserjava.dto;

import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

// 
//   Data Transfer Object destinado ao recebimento de arquivos via formulários Multipart.
//   Utiliza as anotações do RESTEasy Reactive para mapear eficientemente 
//   o binário enviado na requisição HTTP.
//  
public class FileUploadDTO {

    // 
    //  O arquivo binário (imagem da capa) recebido no corpo da requisição.
    //  Contém metadados como nome original, caminho temporário e MIME type.
    // 
    @RestForm("file")
    public FileUpload file;
}
