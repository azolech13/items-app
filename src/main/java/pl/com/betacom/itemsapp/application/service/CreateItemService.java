package pl.com.betacom.itemsapp.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.com.betacom.itemsapp.application.port.in.CreateItem;
import pl.com.betacom.itemsapp.application.port.in.CreateItemCommand;
import pl.com.betacom.itemsapp.application.port.out.ItemRepository;
import pl.com.betacom.itemsapp.domain.Item;

@Service
@RequiredArgsConstructor
@Slf4j
class CreateItemService implements CreateItem {

    private final ItemRepository itemRepository;
    private final CreateItemMapper createItemMapper;

    @Override
    @Transactional
    public Item create(CreateItemCommand command) {
        Item saved = itemRepository.save(createItemMapper.toItem(command));
        log.info("Created item {} for owner {}", saved.id(), saved.ownerId());
        return saved;
    }
}
