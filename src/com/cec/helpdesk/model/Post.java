package com.cec.helpdesk.model;

import com.cec.helpdesk.model.enums.PostType;
import java.sql.Timestamp;

/**
 * Represents a Lost & Found post in the public feed.
 *
 * OOP — ENCAPSULATION: All fields are private; access only via getters/setters.
 */
public class Post {

    private int postId;
    private int authorId;
    private PostType postType;
    private String category;        // e.g. Electronics, ID-Card, Books
    private String title;
    private String description;
    private String imagePath;       // optional
    private String videoPath;       // optional video upload
    private boolean resolved;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // ── transient display helper ────────────────────────────
    private String authorName;

    public Post() {
        this.resolved = false;
    }

    public Post(int authorId, PostType postType, String category,
                String title, String description, String imagePath) {
        this(authorId, postType, category, title, description, imagePath, null);
    }

    public Post(int authorId, PostType postType, String category,
                String title, String description, String imagePath, String videoPath) {
        this.authorId    = authorId;
        this.postType    = postType;
        this.category    = category;
        this.title       = title;
        this.description = description;
        this.imagePath   = imagePath;
        this.videoPath   = videoPath;
        this.resolved    = false;
    }

    // ── getters & setters ───────────────────────────────────
    public int getPostId()                  { return postId; }
    public void setPostId(int postId)       { this.postId = postId; }

    public int getAuthorId()                { return authorId; }
    public void setAuthorId(int authorId)   { this.authorId = authorId; }

    public PostType getPostType()                 { return postType; }
    public void setPostType(PostType postType)    { this.postType = postType; }

    public String getCategory()                 { return category; }
    public void setCategory(String category)    { this.category = category; }

    public String getTitle()              { return title; }
    public void setTitle(String title)    { this.title = title; }

    public String getDescription()                    { return description; }
    public void setDescription(String description)    { this.description = description; }

    public String getImagePath()                  { return imagePath; }
    public void setImagePath(String imagePath)    { this.imagePath = imagePath; }

    public String getVideoPath()                  { return videoPath; }
    public void setVideoPath(String videoPath)    { this.videoPath = videoPath; }

    public boolean isResolved()                   { return resolved; }
    public void setResolved(boolean resolved)     { this.resolved = resolved; }

    public Timestamp getCreatedAt()                 { return createdAt; }
    public void setCreatedAt(Timestamp createdAt)   { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt()                 { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt)   { this.updatedAt = updatedAt; }

    public String getAuthorName()                   { return authorName; }
    public void setAuthorName(String authorName)    { this.authorName = authorName; }

    /**
     * Returns a Reddit-style tag for the post type.
     */
    public String getTypeTag() {
        return "[" + postType.name() + "]";
    }

    @Override
    public String toString() {
        return String.format("%s %s — %s", getTypeTag(), title, category);
    }
}
