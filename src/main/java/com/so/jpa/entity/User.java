package com.so.jpa.entity;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.NaturalId;
import org.hibernate.validator.constraints.Range;
import org.springframework.data.annotation.CreatedDate;

import com.so.jpa.entity.audit.DateAudit;

@Entity
@Table(name = "users", uniqueConstraints = { @UniqueConstraint(columnNames = { "username" }),
@UniqueConstraint(columnNames = { "email" }) })
public class User extends DateAudit {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")
	private Long id;
	
	@Size(max = 40)
	private String name;
	
	@Size(max = 15)
	private String username;
	
	@NaturalId
	@NotBlank
	@Size(max = 40)
	@Email
	private String email;
	
	@Size(max = 100)
	private String password;
	
	private boolean enabled;
	
	@NotNull
    @Enumerated(EnumType.STRING)
    private AuthProvider provider;

	@Column(name = "provider_id")
    private String providerId;
	
	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
	private Set<Role> roles = new HashSet<>();

	@Size(max = 1)
	private String type;
	
	@Size(max = 50)
	private String occupation;
	
	@Size(max = 50)
	private String company_name;
	
	@Range(min=0, max=11)
	private int phone;
	
	@Size(max = 500)
	private String address;
	
	@Size(max = 50)
	private String city;
	
	@Size(max = 50)
	private String country;
	
	@Size(max = 100)
	private String linkedin;
	
	@Size(max = 100)
	private String facebook;
	
	@Size(max = 100)
	private String twitter;
	
	@Size(max = 100)
	private String instagram;
	
	@Size(max = 1)
	private String user_type_cd;
	
	@Size(max = 30)
	private String avatar_img;
	
	private String social_avatar_url;
	
	@CreatedDate
	private Instant join_date;
	
	@Size(max = 500)
	private String note;
	
	public User() {
	}
	public User(String name, String username, String email, String password) {
		this.name = name;
		this.username = username;
		this.email = email;
		this.password = password;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public Set<Role> getRoles() {
		return roles;
	}
	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	public AuthProvider getProvider() {
		return provider;
	}
	public void setProvider(AuthProvider provider) {
		this.provider = provider;
	}
	public String getProviderId() {
		return providerId;
	}
	public void setProviderId(String providerId) {
		this.providerId = providerId;
	}
	public String getSocial_avatar_url() {
		return social_avatar_url;
	}
	public void setSocial_avatar_url(String social_avatar_url) {
		this.social_avatar_url = social_avatar_url;
	}
}
