package com.weather.weatherapp.user;

import com.weather.weatherapp.city.CityEntity;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    // TODO provjeriti ovo
    @ManyToMany
    @JoinTable(
            name = "user_favorite_cities",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "city_id")
    )
    private Set<CityEntity> favoriteCities = new HashSet<>();


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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Set<CityEntity> getFavoriteCities() {
        return favoriteCities;
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
}
