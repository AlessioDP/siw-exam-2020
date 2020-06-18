package it.uniroma3.siw.esame.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@EqualsAndHashCode @ToString
@Getter @Setter
public class Credentials {
	public static final String DEFAULT_ROLE = "DEFAULT";
	public static final String ADMIN_ROLE = "ADMIN";
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Column(unique = true, nullable = false, length = 100)
	private String userName;
	
	@Column(nullable = false, length = 100)
	private String password;
	
	@OneToOne(cascade = CascadeType.ALL)
	@EqualsAndHashCode.Exclude @ToString.Exclude
	private User user;
	
	@Column(nullable = false, length = 10)
	private String role;
	
	@Column(updatable = false, nullable = false)
	private LocalDateTime creationTimestamp;
	
	@Column(nullable = false)
	private LocalDateTime lastUpdateTimestamp;
	
	public Credentials() {}
	
	public Credentials(String userName, String password) {
		this();
		this.userName = userName;
		this.password = password;
	}
	
	public Credentials(String userName, String password, String role, User user) {
		this(userName, password);
		this.role = role;
		this.user = user;
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
