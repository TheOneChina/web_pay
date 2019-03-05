package com.tnsoft.web.service;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.multipart.MultipartFile;

import com.tnsoft.web.model.Response;

public interface HomeService {

	public Response fileUpload(MultipartFile[] files, HttpServletRequest req);

}
