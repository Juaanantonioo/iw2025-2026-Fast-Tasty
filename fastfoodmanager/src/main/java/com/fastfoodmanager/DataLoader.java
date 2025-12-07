import com.fastfoodmanager.domain.Product;
import com.fastfoodmanager.domain.Order;
import com.fastfoodmanager.domain.OrderItem;
import com.fastfoodmanager.domain.User;
import com.fastfoodmanager.domain.User.Role;
import com.fastfoodmanager.service.OrderService;
import com.fastfoodmanager.service.ProductService;
import com.fastfoodmanager.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import com.fastfoodmanager.domain.FoodType;
import com.fastfoodmanager.domain.Allergen;
import com.fastfoodmanager.repository.AllergenRepository;
import com.fastfoodmanager.repository.FoodTypeRepository;


@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private FoodTypeRepository foodTypeRepository;

    @Autowired
    private AllergenRepository allergenRepository;

    @Override
    public void run(String... args) {
        if (foodTypeRepository.count() == 0) {
            foodTypeRepository.save(new FoodType("Entrante"));
            foodTypeRepository.save(new FoodType("Hamburguesa"));
            foodTypeRepository.save(new FoodType("Bebida"));
            foodTypeRepository.save(new FoodType("Postre"));
        }

        if (allergenRepository.count() == 0) {
            allergenRepository.save(new Allergen("Gluten"));
            allergenRepository.save(new Allergen("Lácteos"));
            allergenRepository.save(new Allergen("Huevo"));
            allergenRepository.save(new Allergen("Frutos secos"));
            allergenRepository.save(new Allergen("Soja"));
        }
    }
}
