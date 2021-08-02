package telran.java38.forum.service;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import telran.java38.forum.dao.PostRepository;
import telran.java38.forum.dto.CommentDto;
import telran.java38.forum.dto.NewCommentDto;
import telran.java38.forum.dto.NewPostDto;
import telran.java38.forum.dto.PostDto;
import telran.java38.forum.dto.exceptions.PostNotFoundException;
import telran.java38.forum.model.Comment;
import telran.java38.forum.model.Post;

@Service
public class ForumServiceImpl implements ForumService {

	@Autowired
	PostRepository forumRepository;

	@Override
	public PostDto addNewPost(NewPostDto newPost, String author) {
		Post post = new Post(newPost.getTitle(), newPost.getContent(), author, newPost.getTags());
		post = forumRepository.save(post);
		return convertToPostDto(post);
	}

	@Override
	public PostDto getPost(String id) {
		Post post = forumRepository.findById(id).orElseThrow(() -> new PostNotFoundException(id));
		return convertToPostDto(post);
	}

	@Override
	public PostDto removePost(String id) {
		Post post = forumRepository.findById(id).orElseThrow(() -> new PostNotFoundException(id));
		forumRepository.delete(post);
		return convertToPostDto(post);
	}

	@Override
	public PostDto updatePost(NewPostDto postUpdateDto, String id) {
		Post post = forumRepository.findById(id).orElseThrow(() -> new PostNotFoundException(id));
		String content = postUpdateDto.getContent();
		if (content != null) {
			post.setContent(content);
		}
		String title = postUpdateDto.getTitle();
		if (title != null) {
			post.setTitle(title);
		}
		Set<String> tags = postUpdateDto.getTags();
		if (tags != null) {
			tags.forEach(post::addTag);
		}
		forumRepository.save(post);
		return convertToPostDto(post);
	}

	@Override
	public boolean addLike(String id) {
		Post post = forumRepository.findById(id).orElseThrow(() -> new PostNotFoundException(id));
		post.addLike();
		forumRepository.save(post);
		return true;

	}

	@Override
	public PostDto addComment(String id, String author, NewCommentDto newCommentDto) {
		Post post = forumRepository.findById(id).orElseThrow(() -> new PostNotFoundException(id));
		post.addComment(convertToComment(author, newCommentDto.getMessage()));
		forumRepository.save(post);
		return convertToPostDto(post);
	}

	@Override
	public Iterable<PostDto> findPostsByAuthor(String author) {
		return forumRepository.findByAuthor(author)
				.map(this::convertToPostDto)
				.collect(Collectors.toList());
	}

	private PostDto convertToPostDto(Post post) {
		return PostDto.builder()
				.id(post.getId())
				.author(post.getAuthor())
				.title(post.getTitle())
				.dateCreated(post.getDateCreated())
				.content(post.getContent())
				.tags(post.getTags())
				.likes(post.getLikes())
				.comments(post.getComments().stream().map(this::convertToCommentDto).collect(Collectors.toList()))
				.build();
	}

	private Comment convertToComment(String author, String message) {
		return new Comment(author, message);
	}

	private CommentDto convertToCommentDto(Comment comment) {
		return CommentDto.builder()
				.user(comment.getUser())
				.message(comment.getMessage())
				.dateCreated(comment.getDateCreated()).likes(comment.getLikes()).build();
	}

}
