package com.example.s3withfront.service;

import com.example.s3withfront.bucket.BucketName;
import com.example.s3withfront.filestore.FileStore;
import com.example.s3withfront.model.User;
import com.example.s3withfront.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

import static org.apache.http.entity.ContentType.*;

@Service
public class UserService {

    private final UserRepository repository;
    private final FileStore fileStore;

    public UserService(UserRepository repository, FileStore fileStore) {
        this.repository = repository;
        this.fileStore = fileStore;
    }

    public List<User> getAllUser() {
        return repository.findAll();
    }

    public void uploadUserProfileImage(int userProfileId, MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalStateException("Cannot upload empty file : " + file.getSize());
        }

        if (!Arrays.asList(
                IMAGE_JPEG.getMimeType(),
                IMAGE_PNG.getMimeType(),
                IMAGE_GIF.getMimeType()).contains(file.getContentType())) {
            throw new IllegalStateException("File must be an image");
        }

        User user = findById(userProfileId);

        Map<String, String> metaData = new HashMap<>();
        metaData.put("Content-Type", file.getContentType());
        metaData.put("Content-Length", String.valueOf(file.getSize()));

        String path = String.format("%s/%s", BucketName.PROFILE_IMAGE.getBucketName(), user.getId());
        String fileName = String.format("%s-%s", file.getOriginalFilename(), user.getId());

        try {
            fileStore.save(path, fileName, Optional.of(metaData), file.getInputStream());
            user.setImageLink(fileName);
            repository.save(user);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] downloadUserProfileImage(int id) {
        User user = findById(id);
        String path = String.format("%s/%s", BucketName.PROFILE_IMAGE.getBucketName(), user.getId());
        return user.getImageLink()
                .map(key -> fileStore.download(path, key))
                .orElse(new byte[0]);
    }

    private User findById(int id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalStateException("User not found by id : " + id));
    }
}
