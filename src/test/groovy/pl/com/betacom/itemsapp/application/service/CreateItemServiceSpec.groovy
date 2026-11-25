package pl.com.betacom.itemsapp.application.service

import pl.com.betacom.itemsapp.application.port.out.ItemRepository
import spock.lang.Specification

import static pl.com.betacom.itemsapp.ItemTestConstants.createItemCommand
import static pl.com.betacom.itemsapp.ItemTestConstants.item

class CreateItemServiceSpec extends Specification {

    ItemRepository itemRepository = Mock()
    CreateItemMapper createItemMapper = Mock()
    CreateItemService service = new CreateItemService(itemRepository, createItemMapper)

    def "saves the item created by the mapper"() {
        given:
        def item = item()

        when:
        def saved = service.create(createItemCommand())

        then:
        1 * createItemMapper.toItem(createItemCommand()) >> item
        1 * itemRepository.save(item) >> item
        saved == item
    }
}
