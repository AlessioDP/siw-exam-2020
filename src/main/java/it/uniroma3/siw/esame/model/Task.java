package it.uniroma3.siw.esame.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Entity
@EqualsAndHashCode @ToString
@Getter @Setter
public class Task {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Column(nullable = false, length = 100)
	private String name;
	
	@Column
	private String description;
	
	@Column(nullable = false)
	private boolean completed;
	
	@ManyToOne(fetch = FetchType.EAGER)
	private User user;
	
	@ManyToOne
	@EqualsAndHashCode.Exclude @ToString.Exclude
	private Project project;
	
	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "tasks_tag",
			joinColumns = @JoinColumn(name = "task_id", referencedColumnName = "id"),
			inverseJoinColumns = @JoinColumn(name = "tag_id", referencedColumnName = "id"))
	@EqualsAndHashCode.Exclude @ToString.Exclude
	private Set<Tag> tags;
	
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "task")
	@EqualsAndHashCode.Exclude @ToString.Exclude
	private Set<Comment> comments;
	
	@Column(updatable = false, nullable = false)
	private LocalDateTime creationTimestamp;
	
	@Column(nullable = false)
	@Getter @Setter
	private LocalDateTime lastUpdateTimestamp;
	
	public Task() {
		this.tags = new HashSet<>();
		this.comments = new HashSet<>();
	}
	
	public Task(String name,
				String description,
				boolean completed,
				User user,
				Project project) {
		this();
		this.name = name;
		this.description = description;
		this.completed = completed;
		this.user = user;
		this.project = project;
	}
	
	public Task(String name,
				String description,
				boolean completed,
				User user,
				Project project,
				Tag... tags){
		this(name, description, completed, user, project);
		this.tags.addAll(Arrays.asList(tags));
	}
	
	@PrePersist
	protected void onPersist() {
		this.creationTimestamp = LocalDateTime.now();
		this.lastUpdateTimestamp = LocalDateTime.now();
	}
	
	@PreUpdate
	protected void onUpdate() {
		this.lastUpdateTimestamp = LocalDateTime.now();
	}
	
	public void addTag(Tag tag) {
		this.tags.add(tag);
	}
	
	public void removeTag(Tag tag) {
		this.tags.remove(tag);
	}
	
	public void addComment(Comment comment) {
		this.comments.add(comment);
	}
	
	public void removeComment(Comment comment) {
		this.comments.remove(comment);
	}
}
