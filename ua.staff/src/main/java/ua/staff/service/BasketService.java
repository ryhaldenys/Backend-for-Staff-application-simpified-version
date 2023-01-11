package ua.staff.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.staff.dto.BasketDto;
import ua.staff.exception.NotFoundException;
import ua.staff.exception.ToManyChoseClothesException;
import ua.staff.model.*;
import ua.staff.repository.BasketRepository;
import ua.staff.repository.ChoseClothesRepository;
import ua.staff.repository.ClothesRepository;
import ua.staff.repository.PersonRepository;

import java.math.BigDecimal;
import java.util.ArrayList;

import static java.math.BigDecimal.*;

@Service
@RequiredArgsConstructor
@Transactional
public class BasketService {
    private final ChoseClothesRepository choseClothesRepository;
    private final ClothesRepository clothesRepository;
    private final BasketRepository basketRepository;
    private final PersonRepository personRepository;

    public BasketDto getBasketElements(Long id){
        var basketDtos = basketRepository.findBasketIdAndUsedBonusesById(id);
        var choseClothes = choseClothesRepository.findAllByBasketId(id);
        basketDtos.setClothes(choseClothes);
        return basketDtos;
    }


    public void addClothesToBasket(Long personId, Long clothesId, Size size){
        var basket = getPersonsBasket(personId);
        var foundClothes = getClothes(clothesId);

        checkIsSize(foundClothes,size);
        var choseClothes = createChoseClothes(size,foundClothes);

        saveChoseClothes(basket,choseClothes);
    }


    private Basket getPersonsBasket(Long personId){
        return basketRepository.findById(personId)
                .orElseThrow(()->new NotFoundException("Cannot find person by id: "+personId));
    }

    private Clothes getClothes(Long clothesId){
        return clothesRepository.findById(clothesId)
                .orElseThrow(()->new NotFoundException("Cannot find clothes by id: "+clothesId));
    }

    private void checkIsSize(Clothes clothes, Size choseSize) {
        var clothesId = clothes.getId();
        var sizes = new ArrayList<>(clothesRepository.findClothesSizesById(clothesId));
        sizes.stream().filter(size -> size.getSize().equals(choseSize.getSize()) && size.getAmount()>0)
                .findAny().orElseThrow(()->new NotFoundException("There is no size: "+choseSize));
    }

    private ChoseClothes createChoseClothes(Size size,Clothes foundClothes) {
        var choseClothes = new ChoseClothes(size.getSize());
        choseClothes.addClothes(foundClothes);
        return choseClothes;
    }

    private void saveChoseClothes(Basket basket,ChoseClothes choseClothes){
        basket.addClothesToBasket(choseClothes);
    }




    public void updateAmountOfClothes(Long clothesId,Long personId,Size size) {
        var choseClothes = getChoseClothesByClothesAndPersonIds(clothesId,personId);
        var foundSize = getSizeByIdAndSizeType(choseClothes.getClothes().getId(),size.getSize());

        checkIsEnoughClothes(foundSize,size.getAmount());


        updateAmountOfClothes(choseClothes,size);
    }

    private ChoseClothes getChoseClothesByClothesAndPersonIds(Long id, Long personId) {
        return choseClothesRepository.findByIdAndPersonId(id,personId)
                .orElseThrow(()-> new NotFoundException("Can not find any clothes by clothes id: "+id+" and person id: "+personId));
    }

    private Size getSizeByIdAndSizeType(Long clothesId, String sizeKind) {
        return clothesRepository.findSizeByClothesIdAndSizeType(clothesId,sizeKind)
                .orElseThrow(()-> new NotFoundException("Can not find a size by clothes id: "+clothesId+" and size type: "+sizeKind));
    }

    private void checkIsEnoughClothes(Size size, Integer amountOfClothes) {
        if (amountOfClothes > size.getAmount()){
            throw new ToManyChoseClothesException("You chose to many clothes: "
                                                  +amountOfClothes +",choose less than: "+size.getAmount());
        }
    }

    private void updateAmountOfClothes(ChoseClothes choseClothes, Size size) {
        choseClothes.setAmountOfClothes(size.getAmount());
    }



    public void addBonuses(Long personId) {
        var person = getPersonAndPersonsBasket(personId);
        var totalPrice = getTotalPriceOfClothes(personId);

        addBonusesToBasket(person,totalPrice);
    }
    private Person getPersonAndPersonsBasket(Long personId) {
        return personRepository.findPersonFetchBasketById(personId).orElseThrow(
                ()->new NotFoundException("Cannot find any person bonuses by person id: "+personId));
    }

    private BigDecimal getTotalPriceOfClothes(Long personId) {
        return choseClothesRepository.getTotalPriceOfChoseClothesByBasketId(personId)
                .orElseThrow(() -> new NotFoundException("Cannot find any clothes prices and  discounts by basket id: " + personId));
    }

    private void addBonusesToBasket(Person person, BigDecimal totalPrice) {
        var personBonuses = person.getBonuses();
        var basket = person.getBasket();

        if (totalPrice.compareTo(personBonuses) < 0){
            basket.setUsedBonuses(totalPrice.subtract(valueOf(2)));
        }else {
            basket.setUsedBonuses(person.getBonuses());
        }

    }

}
