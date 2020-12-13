package com.devpedia.watchapedia.service;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.devpedia.watchapedia.exception.ExternalIOException;
import com.devpedia.watchapedia.exception.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class S3Service {

    private AmazonS3 s3Client;

    @Value("${cloud.aws.credentials.accessKey}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secretKey}")
    private String secretKey;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region.static}")
    private String region;

    @PostConstruct
    public void setS3Client() {
        AWSCredentials credentials = new BasicAWSCredentials(this.accessKey, this.secretKey);

        s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(this.region)
                .build();
    }

    /**
     * 이미지를 S3에 업로드한다.
     * @param file 이미지 멀티파트 파일
     * @param filePath S3 Bucket 내의 경로(파일 이름 포함)
     */
    public void upload(MultipartFile file, String filePath) {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(file.getSize());

        try {
            s3Client.putObject(new PutObjectRequest(bucket, filePath, file.getInputStream(), objectMetadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
        } catch (IOException e) {
            throw new ExternalIOException(ErrorCode.INPUT_VALUE_INVALID, "S3 image upload fail");
        }
    }

    /**
     * 이미지를 S3에 업로드한다.
     * @param file 이미지 파일
     * @param filePath S3 Bucket 내의 경로(파일 이름 포함)
     */
    public void upload(File file, String filePath) {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(file.length());

        try {
            s3Client.putObject(new PutObjectRequest(bucket, filePath, new FileInputStream(file), objectMetadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
        } catch (IOException e) {
            throw new ExternalIOException(ErrorCode.INPUT_VALUE_INVALID, "S3 image upload fail");
        }
    }
}
