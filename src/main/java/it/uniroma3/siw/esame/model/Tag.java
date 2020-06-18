package it.uniroma3.siw.esame.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@EqualsAndHashCode @ToString
@Getter @Setter
public class Tag {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Column(nullable = false, length = 100)
	private String name;
	
	@Column
	private String description;
	
	@Column
	private String color;
	
	@ManyToOne
	@EqualsAndHashCode.Exclude @ToString.Exclude
	private Project project;
	
	@ManyToMany(fetch = FetchType.LAZY, mappedBy = "tags")
	@EqualsAndHashCode.Exclude @ToString.Exclude
	private Set<Task> tasks;
	
	@Column(updatable = false, nullable = false)
	private LocalDateTime creationTimestamp;
	
	@Column(nullable = false)
	private LocalDateTime lastUpdateTimestamp;
	
	public Tag() {
		tasks = new HashSet<>();
	}
	
	public Tag(String name,
			   String description,
			   String color) {
		this.name = name;
		this.description = description;
		this.color = color;
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
