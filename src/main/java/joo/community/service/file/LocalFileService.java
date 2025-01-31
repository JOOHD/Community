package joo.community.service.file;

import joo.community.exception.FileUploadFailureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;

@Service
@Slf4j
public class LocalFileService implements FileService {

    @Value("${upload.image.location}")
    private String location;

    @PostConstruct
    void postConstruct() {
        File dir = new File(location);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();  // 디렉토리가 없다면 생성
            if (!created) {
                log.error("디렉토리 생성 실패: " + location);
            }
        }
    }

    @Override
    public void upload(MultipartFile file, String filename) {
        try {
            File targetFile = new File(location + File.separator + filename);  // 경로 + 파일명
            file.transferTo(targetFile);  // 지정된 경로로 파일 저장
            log.info("파일 업로드 성공: " + targetFile.getPath());
        } catch (IOException e) {
            throw new FileUploadFailureException(e);
        }
    }

    @Override
    public void delete(String filename) {
        File targetFile = new File(location + File.separator + filename);
        if (targetFile.exists()) {
            boolean deleted = targetFile.delete();  // 파일 삭제
            if (deleted) {
                log.info("파일 삭제 성공: " + targetFile.getPath());
            } else {
                log.error("파일 삭제 실패: " + targetFile.getPath());
            }
        }
    }
}