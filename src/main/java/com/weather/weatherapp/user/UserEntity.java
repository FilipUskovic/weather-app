package com.weather.weatherapp.user;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @ElementCollection
    @CollectionTable(name = "favorite_cities", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "city")
    private Set<String> favoriteCities = new HashSet<>();

    public UserEntity(Long id, String username, Set<String> favoriteCities) {
        this.id = id;
        this.username = username;
        this.favoriteCities = favoriteCities;
    }

    public UserEntity() {
    }

    public UserEntity(String username) {
        this.username = username;
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

    public Set<String> getFavoriteCities() {
        return favoriteCities;
    }

    public void setFavoriteCities(Set<String> favoriteCities) {
        this.favoriteCities = favoriteCities;
    }

    public void addFavoriteCity(String city) {
        this.favoriteCities.add(city);
    }

    public void removeFavoriteCity(String city) {
        this.favoriteCities.remove(city);
    }
}
