package it.uniroma3.siw.esame.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@EqualsAndHashCode
@Getter @Setter
public class Project {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Column(nullable = false, length = 100)
	private String name;
	
	@Column
	private String description;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@EqualsAndHashCode.Exclude @ToString.Exclude
	private User owner;
	
	@ManyToMany(fetch = FetchType.LAZY)
	@EqualsAndHashCode.Exclude @ToString.Exclude
	private List<User> members;
	
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "project")
	@EqualsAndHashCode.Exclude @ToString.Exclude
	private Set<Task> tasks;
	
	/*@OneToMany(fetch = FetchType.LAZY,        // whenever a Project is retrieved, always retrieve its tags too
			cascade = CascadeType.ALL,      // if a Project is deleted, all its tags must be deleted too
			orphanRemoval = true)
	@JoinColumn(name="project_id")*/
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "project")
	@EqualsAndHashCode.Exclude @ToString.Exclude
	private Set<Tag> tags;
	
	@Column(updatable = false, nullable = false)
	private LocalDateTime creationTimestamp;
	
	@Column(nullable = false)
	private LocalDateTime lastUpdateTimestamp;
	
	public Project() {
		this.members = new ArrayList<>();
		this.tasks = new HashSet<>();
		this.tags = new HashSet<>();
	}
	
	public Project(String name, String description) {
		this();
		this.name = name;
		this.description = description;
	}
	
	public void addMember(User user) {
		if (!this.members.contains(user))
			this.members.add(user);
	}
	
	public void removeMember(User user) {
		this.members.remove(user);
	}
	
	public void addTask(Task task) {
		if (!getTasks().contains(task))
			getTasks().add(task);
	}
	
	public void removeTask(Task task) {
		this.tasks.remove(task);
	}
	
	public void addTag(Tag tag) {
		if (!getTags().contains(tag))
			getTags().add(tag);
	}
	
	public void removeTag(Tag tag) {
		this.tags.remove(tag);
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
	
	@Override
	public String toString() {
		return "";
	}
}
