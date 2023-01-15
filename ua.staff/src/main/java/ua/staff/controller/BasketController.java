package ua.staff.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.staff.builder.UriBuilder;
import ua.staff.dto.BasketDto;
import ua.staff.model.Size;
import ua.staff.service.BasketService;

import static ua.staff.generator.ResponseEntityGenerator.getResponseEntityWithNoContent;

@RestController
@RequestMapping("/people/{person_id}/basket")
@RequiredArgsConstructor
public class BasketController {
    private final BasketService basketService;

    @GetMapping
    public BasketDto getBasket(@PathVariable Long person_id){
        return basketService.getBasketElements(person_id);
    }

    @PostMapping("/{clothes_id}")
    public ResponseEntity<Void> addToBasket(
            @PathVariable Long person_id,
            @PathVariable("clothes_id")Long clothes_id, @RequestBody Size size){

        basketService.addClothesToBasket(person_id,clothes_id,size);
        var location = UriBuilder.createUriFromCurrentServletMapping("/people/{p_id}/basket",person_id);
        return getResponseEntityWithNoContent(location);
    }

    @DeleteMapping("/{chose_clothes_id}")
    public ResponseEntity<Void> removeChoseClothes(@PathVariable("person_id")Long personId,
            @PathVariable("chose_clothes_id")Long choseClothesId){

        basketService.removeChoseClothesById(choseClothesId);

        var location = UriBuilder.createUriFromCurrentServletMapping("/people/{p_id}/basket",personId);
        return getResponseEntityWithNoContent(location);
    }

    @PatchMapping("/{chose_clothes_id}")
    public ResponseEntity<Void> updateAmountOfClothes(
            @RequestBody Size size, @PathVariable Long person_id,@PathVariable Long chose_clothes_id){

        basketService.updateAmountOfClothes(chose_clothes_id,person_id,size);

        var location = UriBuilder.createUriFromCurrentServletMapping("/people/{p_id}/basket",person_id);
        return getResponseEntityWithNoContent(location);
    }

    @PatchMapping
    public ResponseEntity<Void> addPersonBonuses(@PathVariable("person_id") Long personId){
        basketService.addBonuses(personId);

        var location = UriBuilder.createUriFromCurrentRequest();
        return getResponseEntityWithNoContent(location);
    }

    @DeleteMapping
    public ResponseEntity<Void> removePersonBonuses(@PathVariable("person_id") Long personId){

        basketService.removePersonBonuses(personId);

        var location = UriBuilder.createUriFromCurrentRequest();
        return getResponseEntityWithNoContent(location);
    }

}
