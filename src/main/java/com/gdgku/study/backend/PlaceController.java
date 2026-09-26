package com.gdgku.study.backend;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/places")
public class PlaceController {

    private final List<Place> places = new ArrayList<>(List.of(
            new Place(1L, "고려대학교 중앙광장", "university", 37.5895, 127.0324),
            new Place(2L, "카페 A", "cafe", 37.5885, 127.0335),
            new Place(3L, "식당 B", "restaurant", 37.5900, 127.0315)
    ));

    // 1. 모든 장소 조회
    // GET /places
    //
    // 카테고리가 있으면 해당 카테고리만 조회
    // GET /places?category=cafe
    @GetMapping
    public List<Place> getPlaces(
            @RequestParam(required = false) String category) {

        if (category == null) {
            return places;
        }

        List<Place> result = new ArrayList<>();

        for (Place place : places) {
            if (place.getCategory().equalsIgnoreCase(category)) {
                result.add(place);
            }
        }

        return result;
    }

    // 2. 특정 장소 조회
    // GET /places/1
    @GetMapping("/{id}")
    public ResponseEntity<Place> getPlace(
            @PathVariable Long id) {

        for (Place place : places) {
            if (place.getId().equals(id)) {
                return ResponseEntity.ok(place);
            }
        }

        return ResponseEntity.notFound().build();
    }

    // 3. 새로운 장소 생성
    // POST /places
    @PostMapping
    public ResponseEntity<Place> createPlace(
            @RequestBody Place newPlace) {

        long newId = places.stream()
                .mapToLong(Place::getId)
                .max()
                .orElse(0L) + 1;

        newPlace.setId(newId);

        places.add(newPlace);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(newPlace);
    }

    // 4. 특정 장소 삭제
    // DELETE /places/1
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlace(
            @PathVariable Long id) {

        for (Place place : places) {
            if (place.getId().equals(id)) {
                places.remove(place);

                return ResponseEntity.noContent().build();
            }
        }

        return ResponseEntity.notFound().build();
    }
}