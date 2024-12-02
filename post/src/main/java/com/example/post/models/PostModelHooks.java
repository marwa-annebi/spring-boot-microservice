package com.example.post.models;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterDeleteEvent;
import org.springframework.stereotype.Component;

@Component
public class PostModelHooks extends AbstractMongoEventListener<Post> {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void onAfterDelete(AfterDeleteEvent<Post> event) {
        String postId = event.getSource().get("_id").toString();
        // Perform actions after deleting the post
        System.out.println("Post with ID " + postId + " has been deleted.");
    }
}
