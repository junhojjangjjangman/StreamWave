package com.bipa4.back_bipatv.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.cloudfront.CloudFrontUrlSigner;
import com.amazonaws.services.cloudfront.util.SignerUtils;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.Headers;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.bipa4.back_bipatv.dataType.ErrorCode;
import com.bipa4.back_bipatv.dto.video.GetFileUrlResponseDto;
import com.bipa4.back_bipatv.dto.video.GetUrlResponseDto;
import com.bipa4.back_bipatv.exception.CustomApiException;
import java.io.File;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class PresignedUrlService {

  @Value("${cloud.aws.s3.bucket}")
  private String bucket;

  @Value("${cloud.aws.cloudFront.distributionDomain}")
  private String distributionDomain;

  @Value("${cloud.aws.cloudFront.keyPairId}")
  private String keyPairId;

  @Value("${cloud.aws.path}")
  private String path;

  private final AmazonS3 amazonS3;

  public GetFileUrlResponseDto getPreSignedUrl(String fileName, String folderName) {

    // 파일이름
    String fileId = UUID.randomUUID().toString();
    String fileKey = folderName + "/" + fileName + "/" + fileId + fileName;

    // 만료 기간 정하기
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(new Date());
    calendar.add(Calendar.MINUTE, 10);

    // presigned-url 생성
    GeneratePresignedUrlRequest generateVideoPresignedUrlImageRequest =
        new GeneratePresignedUrlRequest(bucket, fileKey)
            .withMethod(HttpMethod.PUT)
            .withExpiration(calendar.getTime());

    generateVideoPresignedUrlImageRequest.addRequestParameter(
        Headers.S3_CANNED_ACL,
        CannedAccessControlList.PublicRead.toString());

    String presignedUrl = amazonS3.generatePresignedUrl(generateVideoPresignedUrlImageRequest)
        .toString();

    if (presignedUrl == null) {
      throw new CustomApiException(ErrorCode.PRESIGNED_URL_ERROR);
    }

    return new GetFileUrlResponseDto(presignedUrl, fileKey);
  }


  public GetUrlResponseDto getPreSignedUrlCDN(String videoName, String imageName) {
    GetUrlResponseDto requestDto = null;

    try {
      SignerUtils.Protocol protocol = SignerUtils.Protocol.http;
      File privateKeyFile = ResourceUtils.getFile(path);
      String s3VideoKey =
          videoName.split("[.]")[0] + LocalDateTime.now() + "." + videoName.split("[.]")[1];
      String s3ImageKey =
          imageName.split("[.]")[0] + LocalDateTime.now() + "." + imageName.split("[.]")[1];

      Date expirationTime = new Date(System.currentTimeMillis() + 3600000); // 1 hour from now

      String signedVideoURL = CloudFrontUrlSigner.getSignedURLWithCannedPolicy(
          protocol,
          distributionDomain,
          privateKeyFile,
          s3VideoKey,
          keyPairId,
          expirationTime
      );

      String signedImageURL = CloudFrontUrlSigner.getSignedURLWithCannedPolicy(
          protocol,
          distributionDomain,
          privateKeyFile,
          s3ImageKey,
          keyPairId,
          expirationTime
      );

      requestDto = new GetUrlResponseDto(signedVideoURL, signedImageURL, s3VideoKey, s3ImageKey);

    } catch (Exception e) {
      throw new CustomApiException(ErrorCode.PRESIGNED_URL_ERROR);
    }

    return requestDto;
  }
}
