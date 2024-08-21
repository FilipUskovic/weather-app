package com.weather.weatherapp.user;

import com.weather.weatherapp.city.CityEntity;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Entity
@Table(name = "user")
public class UserEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    //unique = true,
    @Column(nullable = false)
    private String username;

    // TODO provjeriti ovo
    // dodao lazy fetch
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_favorite_cities",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "city_id")
    )
    private Set<CityEntity> favoriteCities = new HashSet<>();


    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    private Role role;

    public UserEntity() {}

    public UserEntity(String username) {
        this.username = username;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
 // // Koristim email kao username za Spring Security
    public String getUsername() {
        return email;
    }

    public String getRealUsername() {
        return username;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Set<CityEntity> getFavoriteCities() {
        if (favoriteCities == null) {
            favoriteCities = new HashSet<>();
        }
        return favoriteCities;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void setFavoriteCities(Set<CityEntity> favoriteCities) {
        this.favoriteCities = favoriteCities;
    }

    // Pomoćne metode za upravljanje vezama
    public void addFavoriteCity(CityEntity city) {
        favoriteCities.add(city);
        city.getUsers().add(this);
    }

    public void removeFavoriteCity(CityEntity city) {
        favoriteCities.remove(city);
        city.getUsers().remove(this);
    }



    // equals, hashCode i toString metode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserEntity that)) return false;
        return Objects.equals(getId(), that.getId()) &&
                Objects.equals(getUsername(), that.getUsername()) &&
                Objects.equals(getEmail(), that.getEmail());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getUsername(), getEmail());
    }

    @Override
    public String toString() {
        return "UserEntity{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                '}';
    }
}
