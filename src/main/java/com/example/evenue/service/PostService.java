package com.example.evenue.service;

import com.example.evenue.models.events.EventDao;
import com.example.evenue.models.events.EventModel;
import com.example.evenue.models.events.EventResponse;
import com.example.evenue.models.posts.*;
import com.example.evenue.models.users.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostService {

    @Autowired
    private PostDao postDao;

    @Autowired
    private PostImageDao postImageDao;

    @Autowired
    private FileStorageService fileStorageService;  // Handles Base64 conversion

    @Autowired
    private EventDao eventDao;

    /**
     * Create a new post with optional images converted to Base64.
     *
     * @param content   The content of the post.
     * @param eventId   The ID of the event associated with the post.
     * @param user      The user creating the post.
     * @param images    A list of optional images to be converted to Base64.
     * @return          The created post with Base64-encoded images.
     */
    public PostModel createPost(String title, String content, Long eventId, UserModel user, List<MultipartFile> images) {
        // Step 1: Retrieve the EventModel by eventId
        EventModel event = eventDao.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        // Step 2: Create and save the post
        PostModel post = new PostModel();
        post.setTitle(title);
        post.setContent(content);
        post.setEvent(event);  // Set the EventModel object instead of eventId
        post.setUser(user);
        post.setCreatedAt(LocalDateTime.now());
        postDao.save(post);

        // Step 3: Save Base64 images if they are provided
        if (images != null && !images.isEmpty()) {
            for (MultipartFile image : images) {
                String base64Image = fileStorageService.convertToBase64AndStore(image);

                if (base64Image != null) {
                    PostImageModel postImage = new PostImageModel();
                    postImage.setPost(post);
                    postImage.setImageUrl(base64Image); // Save the Base64 string
                    postImage.setCreatedAt(LocalDateTime.now());
                    postImageDao.save(postImage);
                }
            }
        }

        return post;
    }

    /**
     * Get all posts along with their Base64-encoded images.
     *
     * @return A list of post responses containing Base64-encoded images.
     */
    public List<PostResponse> getAllPosts() {
        List<PostModel> posts = postDao.findAll();
        List<PostResponse> postResponses = new ArrayList<>();

        for (PostModel post : posts) {
            PostResponse postResponse = new PostResponse();
            postResponse.setId(post.getId());
            postResponse.setTitle(post.getTitle());
            postResponse.setContent(post.getContent());
            postResponse.setCreatedAt(post.getCreatedAt());

            // Get associated Base64 images for each post
            List<PostImageModel> images = postImageDao.findByPostId(post.getId());
            List<String> imageUrls = images.stream()
                    .map(PostImageModel::getImageUrl) // Base64 string here
                    .collect(Collectors.toList());

            postResponse.setImageUrls(imageUrls);
            postResponses.add(postResponse);
        }

        return postResponses;
    }

    public List<PostResponse> getUpcomingPosts() {
        List<PostModel> allPosts = postDao.findAll();
        return allPosts.stream()
                .filter(post -> post.getEvent().isUpcoming()) // Filter by upcoming events
                .map(post -> {
                    PostResponse response = new PostResponse();
                    response.setId(post.getId());
                    response.setTitle(post.getTitle());
                    response.setContent(post.getContent());
                    response.setCreatedAt(post.getCreatedAt());
//                    response.setImageUrls(post.getImagesUrls()); // Assuming you have an image handling method
                    return response;
                })
                .collect(Collectors.toList());
    }


    /**
     * Retrieve a post by its ID along with Base64-encoded images.
     *
     * @param postId The ID of the post to retrieve.
     * @return       The post response containing Base64-encoded images.
     */
    public PostResponse getPostById(Long postId) {
        // Fetch the post from the database or throw an exception if not found
        PostModel post = postDao.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // Create a new PostResponse object
        PostResponse postResponse = new PostResponse();
        postResponse.setId(post.getId());
        postResponse.setTitle(post.getTitle());
        postResponse.setContent(post.getContent());
        postResponse.setCreatedAt(post.getCreatedAt());

        // Get associated Base64 images for this post
        List<PostImageModel> images = postImageDao.findByPostId(post.getId());
        List<String> imageUrls = images.stream()
                .map(PostImageModel::getImageUrl) // Base64 string here
                .collect(Collectors.toList());

        postResponse.setImageUrls(imageUrls);

        // Check if the post has an associated event
        if (post.getEvent() != null) {
            // Create and set the event details in the response
            EventResponse eventResponse = new EventResponse();
            eventResponse.setId(post.getEvent().getId());
            eventResponse.setEventName(post.getEvent().getEventName());

            eventResponse.setLocation(post.getEvent().getLocation());
            eventResponse.setDate(post.getEvent().getEventDate().atTime(post.getEvent().getStartTime())); // Combine date and time
            eventResponse.setDescription(post.getEvent().getDescription());
            eventResponse.setOrganizerName(post.getEvent().getOrganizerName());
            eventResponse.setUpcoming(post.getEvent().isUpcoming()); // Set whether the event is upcoming

            // Set the event in the post response
            postResponse.setEvent(eventResponse);

            // Log the event ID to verify the event is fetched
            System.out.println("Event ID: " + post.getEvent().getId());
        } else {
            System.out.println("No event associated with this post.");
        }

        return postResponse;
    }


    public List<PostResponse> getPostsByUserId(Long userId) {
        // Step 1: Retrieve posts by userId
        List<PostModel> posts = postDao.findByUserId(userId);
        List<PostResponse> postResponses = new ArrayList<>();

        // Step 2: Create PostResponse objects for each post
        for (PostModel post : posts) {
            PostResponse postResponse = new PostResponse();
            postResponse.setId(post.getId());
            postResponse.setTitle(post.getTitle());
            postResponse.setContent(post.getContent());
            postResponse.setCreatedAt(post.getCreatedAt());

            // Get associated Base64 images for each post
            List<PostImageModel> images = postImageDao.findByPostId(post.getId());
            List<String> imageUrls = images.stream()
                    .map(PostImageModel::getImageUrl) // Base64 string here
                    .collect(Collectors.toList());

            postResponse.setImageUrls(imageUrls);
            postResponses.add(postResponse);
        }

        return postResponses;
    }

    public PostResponse buildPostResponse(PostModel post) {
        PostResponse response = new PostResponse();
        response.setId(post.getId());
        response.setTitle(post.getTitle());
        response.setContent(post.getContent());
        response.setCreatedAt(post.getCreatedAt());

        // Set image URLs
        List<String> imageUrls = post.getImages().stream()
                .map(PostImageModel::getImageUrl)
                .collect(Collectors.toList());
        response.setImageUrls(imageUrls);

        // Set event details if present
        if (post.getEvent() != null) {
            EventResponse eventResponse = new EventResponse();
            eventResponse.setId(post.getEvent().getId());
            eventResponse.setEventName(post.getEvent().getEventName());
            eventResponse.setLocation(post.getEvent().getLocation());
            eventResponse.setDate(post.getEvent().getEventDate().atTime(post.getEvent().getStartTime())); // Combine date and time
            eventResponse.setDescription(post.getEvent().getDescription());
            eventResponse.setOrganizerName(post.getEvent().getOrganizerName());
            eventResponse.setUpcoming(post.getEvent().isUpcoming());  // Set whether the event is upcoming

            response.setEvent(eventResponse);
        }

        return response;
    }

    public void updatePost(Long postId, String title, String content, Long eventId, List<MultipartFile> images, List<String> removeImages) {
        PostModel post = postDao.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));

        // Update the post's title and content
        post.setTitle(title); // Update the title
        post.setContent(content);
        post.setEvent(eventDao.findById(eventId).orElseThrow(() -> new RuntimeException("Event not found")));

        // Remove selected images
        if (removeImages != null && !removeImages.isEmpty()) {
            for (String imageUrl : removeImages) {
                PostImageModel image = postImageDao.findByImageUrlAndPost(imageUrl, post)
                        .orElseThrow(() -> new RuntimeException("Image not found"));
                postImageDao.delete(image);
            }
        }

        // Add new images
        if (images != null && !images.isEmpty()) {
            for (MultipartFile image : images) {
                String base64Image = fileStorageService.convertToBase64AndStore(image);
                PostImageModel postImage = new PostImageModel();
                postImage.setPost(post);
                postImage.setImageUrl(base64Image);
                postImageDao.save(postImage);
            }
        }

        // Save the updated post
        postDao.save(post);
    }

    public void deletePost(Long postId) {
        postDao.deleteById(postId);
    }
}
