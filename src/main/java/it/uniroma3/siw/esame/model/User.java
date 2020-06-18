package it.uniroma3.siw.esame.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@EqualsAndHashCode @ToString
@Getter @Setter
public class User {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Column(nullable = false, length = 100)
	private String firstName;
	
	@Column(nullable = false, length = 100)
	private String lastName;
	
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "owner")
	@EqualsAndHashCode.Exclude @ToString.Exclude
	private List<Project> ownedProjects;
	
	@ManyToMany(mappedBy = "members")
	@EqualsAndHashCode.Exclude @ToString.Exclude
	private List<Project> visibleProjects;
	
	@Column(updatable = false, nullable = false)
	private LocalDateTime creationTimestamp;
	
	@Column(nullable = false)
	private LocalDateTime lastUpdateTimestamp;
	
	public User() {
		this.ownedProjects = new ArrayList<>();
		this.visibleProjects = new ArrayList<>();
	}
	
	public User(String firstName, String lastName) {
		this();
		this.firstName = firstName;
		this.lastName = lastName;
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
}
