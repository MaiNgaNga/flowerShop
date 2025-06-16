package poly.edu.Assignment.Service;

import java.util.List;

import poly.edu.Assignment.model.Comment;

public interface CommentService {
     List<Comment> getCommentsByProduct(Long productId);

     Comment saveComment(String content, Long productId);

     void deleteComment(Long id);
}
