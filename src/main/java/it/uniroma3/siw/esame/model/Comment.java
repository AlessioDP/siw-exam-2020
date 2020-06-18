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
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import java.time.LocalDateTime;

@Entity
@EqualsAndHashCode
@ToString
@Getter @Setter
public class Comment {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Column(nullable = false, length = 100)
	private String text;
	
	@ManyToOne(fetch = FetchType.EAGER)
	private User user;
	
	@ManyToOne
	@EqualsAndHashCode.Exclude @ToString.Exclude
	private Task task;
	
	@Column(updatable = false, nullable = false)
	private LocalDateTime creationTimestamp;
	
	public Comment() {}
	
	public Comment(String text, User user, Task task) {
		this();
		this.text = text;
		this.user = user;
		this.task = task;
	}
	
	@PrePersist
	protected void onPersist() {
		this.creationTimestamp = LocalDateTime.now();
	}
}
