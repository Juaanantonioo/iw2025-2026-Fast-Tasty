package com.fastfoodmanager.views;

import com.fastfoodmanager.domain.Product;
import com.fastfoodmanager.domain.Allergen;
import com.fastfoodmanager.domain.FoodType;
import com.fastfoodmanager.repository.AllergenRepository;
import com.fastfoodmanager.service.ProductService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.validator.DoubleRangeValidator;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Route(value = "products", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class ProductView extends VerticalLayout {

    private final ProductService service;
    private final AllergenRepository allergenRepo;

    // Formulario
    private final TextField name = new TextField("Nombre");
    private final TextArea description = new TextArea("Descripción");
    private final ComboBox<FoodType> type = new ComboBox<>("Tipo");
    private final MultiSelectComboBox<Allergen> allergens = new MultiSelectComboBox<>("Alérgenos");
    private final NumberField price = new NumberField("Precio (€)");
    private final Checkbox active = new Checkbox("Activo", true);

    // Ingredientes
    private final VerticalLayout ingredientsContainer = new VerticalLayout();
    private final Button addIngredientBtn = new Button("➕ Agregar ingrediente");

    // Imagen
    private final MemoryBuffer imageBuffer = new MemoryBuffer();
    private final Upload imageUpload = new Upload(imageBuffer);
    private final Image previewImage = new Image();

    // Botones
    private final Button save = new Button("Guardar");
    private final Button clear = new Button("Limpiar");
    private final Button delete = new Button("Eliminar seleccionado");

    // Grid
    private final Grid<Product> grid = new Grid<>(Product.class, false);

    private final Binder<Product> binder = new Binder<>(Product.class);
    private Product current = new Product();

    public ProductView(ProductService service, AllergenRepository allergenRepo) {
        this.service = service;
        this.allergenRepo = allergenRepo;

        setSizeFull();
        setPadding(true);
        setSpacing(false);
        addClassName("products-admin-view");

        // Cabecera
        var header = new H1("Gestión de productos");
        header.getStyle()
                .set("margin", "0")
                .set("font-weight", "800")
                .set("color", "#1f2937");

        // Botones estilizados
        stylePrimary(save);
        styleTertiary(clear);
        styleError(delete);

        HorizontalLayout actionsBar = new HorizontalLayout(save, clear, delete);
        actionsBar.setAlignItems(Alignment.CENTER);
        actionsBar.getStyle().set("gap", "10px");

        // ===== FORMULARIO =====
        description.setMaxLength(800);
        description.setHelperText("Máx. 800 caracteres");
        description.setWidthFull();

        price.setStep(0.10);
        price.setMin(0.0);
        price.setClearButtonVisible(true);
        price.setWidth("180px");

        name.setClearButtonVisible(true);
        name.setWidth("280px");

        type.setItems(service.findAllFoodTypes());
        type.setItemLabelGenerator(FoodType::getName);
        type.setPlaceholder("Selecciona el tipo");

        allergens.setItems(allergenRepo.findAll());
        allergens.setItemLabelGenerator(Allergen::getName);
        allergens.setPlaceholder("Selecciona alérgenos si los tiene");

        // Ingredientes
        ingredientsContainer.setPadding(false);
        ingredientsContainer.setSpacing(true);
        addIngredientBtn.addClickListener(e -> addIngredientRow(null, null, false));

        // Configuración Upload de imagen
        imageUpload.setAcceptedFileTypes("image/jpeg", "image/png");
        imageUpload.setMaxFiles(1);
        imageUpload.setDropLabel(new Span("Arrastra o selecciona la imagen del producto"));
        imageUpload.setWidthFull();

        previewImage.setWidth("200px");
        previewImage.setHeight("150px");

        imageUpload.addSucceededListener(event -> {
            try (InputStream is = imageBuffer.getInputStream()) {
                BufferedImage bufferedImage = ImageIO.read(is);
                if (bufferedImage == null) {
                    Notification.show("Formato de imagen no válido", 3000, Notification.Position.TOP_CENTER);
                    imageUpload.clearFileList();
                    previewImage.setSrc("");
                    return;
                }
                int width = bufferedImage.getWidth();
                int height = bufferedImage.getHeight();
                if (width != 400 || height != 300) {
                    Notification.show("La imagen debe tener 400x300 px", 3000, Notification.Position.TOP_CENTER);
                    imageUpload.clearFileList();
                    previewImage.setSrc("");
                    return;
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(bufferedImage, "png", baos);
                String base64 = Base64.getEncoder().encodeToString(baos.toByteArray());
                previewImage.setSrc("data:image/png;base64," + base64);
            } catch (Exception ex) {
                Notification.show("Error al procesar la imagen", 3000, Notification.Position.TOP_CENTER);
                imageUpload.clearFileList();
                previewImage.setSrc("");
            }
        });

        var form = new com.vaadin.flow.component.formlayout.FormLayout();
        form.setResponsiveSteps(
                new com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep("0", 1),
                new com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep("720px", 2)
        );

        form.add(name, type, price, description, allergens, active,
                addIngredientBtn, ingredientsContainer, imageUpload, previewImage);

        form.setColspan(description, 2);
        form.setColspan(allergens, 2);
        form.setColspan(addIngredientBtn, 2);
        form.setColspan(ingredientsContainer, 2);
        form.setColspan(imageUpload, 2);
        form.setColspan(previewImage, 2);

        Div formCard = new Div(form, actionsBar);
        formCard.getStyle()
                .set("background", "white")
                .set("border-radius", "14px")
                .set("padding", "16px")
                .set("box-shadow", "0 2px 10px rgba(0,0,0,.06)")
                .set("margin-bottom", "14px");

        // ===== GRID =====
        grid.addColumn(Product::getId).setHeader("Id").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(Product::getName).setHeader("Nombre").setAutoWidth(true);
        grid.addColumn(p -> p.getType() != null ? p.getType().getName() : "")
                .setHeader("Tipo").setAutoWidth(true);
        grid.addColumn(Product::getDescription).setHeader("Descripción").setAutoWidth(true).setFlexGrow(2);
        grid.addColumn(p -> formatMoney(p.getPrice())).setHeader("Precio").setAutoWidth(true);
        grid.addColumn(p -> p.getAllergens() == null ? "" :
                        p.getAllergens().stream().map(Allergen::getName).collect(Collectors.joining(", ")))
                .setHeader("Alérgenos").setAutoWidth(true);
        grid.addColumn(Product::isActive).setHeader("Activo").setAutoWidth(true);

        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COMPACT, GridVariant.LUMO_WRAP_CELL_CONTENT);
        grid.setHeight("55vh");

        grid.asSingleSelect().addValueChangeListener(ev -> {
            var sel = ev.getValue();
            if (sel != null) {
                current = sel;
                binder.readBean(current);

                ingredientsContainer.removeAll();
                if (current.getIngredients() != null) {
                    current.getIngredients().forEach(i ->
                            addIngredientRow(i.getName(), i.getQuantity(), i.isCustomizable())
                    );
                }

                if (current.getImage() != null) {
                    String base64 = Base64.getEncoder().encodeToString(current.getImage());
                    previewImage.setSrc("data:image/png;base64," + base64);
                } else previewImage.setSrc("");
            } else {
                resetForm();
            }
        });

        add(header, formCard, grid);

        // ===== BINDER =====
        binder.forField(name).asRequired("El nombre es obligatorio").bind(Product::getName, Product::setName);
        binder.forField(description).bind(Product::getDescription, Product::setDescription);
        binder.forField(type).asRequired("Debes elegir un tipo de comida").bind(Product::getType, Product::setType);
        binder.forField(allergens).bind(Product::getAllergens, Product::setAllergens);
        binder.forField(price).asRequired("El precio es obligatorio")
                .withValidator(new DoubleRangeValidator("El precio debe ser ≥ 0", 0.0, null))
                .bind(Product::getPrice, Product::setPrice);
        binder.forField(active).bind(Product::isActive, Product::setActive);

        save.addClickListener(e -> onSave());
        clear.addClickListener(e -> resetForm());
        delete.addClickListener(e -> {
            var sel = grid.asSingleSelect().getValue();
            if (sel == null) {
                Notification.show("Selecciona una fila para eliminar");
                return;
            }
            service.delete(sel.getId());
            Notification.show("Producto eliminado");
            load();
            resetForm();
        });

        load();
    }

    private void addIngredientRow(String nameVal, Double quantityVal, boolean customizableVal) {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setAlignItems(Alignment.CENTER);
        row.getStyle().set("gap", "10px");

        TextField ingredientName = new TextField();
        ingredientName.setPlaceholder("Ingrediente");
        ingredientName.setWidth("200px");
        if (nameVal != null) ingredientName.setValue(nameVal);

        NumberField ingredientQty = new NumberField();
        ingredientQty.setPlaceholder("Cantidad (0-3)");
        ingredientQty.setMin(0);
        ingredientQty.setMax(3);
        ingredientQty.setStep(0.1);
        ingredientQty.setWidth("100px");
        if (quantityVal != null) ingredientQty.setValue(quantityVal);

        Checkbox customizable = new Checkbox("Personalizable");
        customizable.setValue(customizableVal);

        Button removeBtn = new Button("❌", e -> ingredientsContainer.remove(row));

        row.add(ingredientName, ingredientQty, customizable, removeBtn);
        ingredientsContainer.add(row);
    }

    private void onSave() {
        try {
            binder.writeBean(current);

            List<Product.Ingredient> ingredientsList = ingredientsContainer.getChildren()
                    .map(c -> (HorizontalLayout) c)
                    .map(row -> {
                        TextField nameField = (TextField) row.getComponentAt(0);
                        NumberField qtyField = (NumberField) row.getComponentAt(1);
                        Checkbox customField = (Checkbox) row.getComponentAt(2);

                        if (nameField.getValue() == null || nameField.getValue().isEmpty()) return null;

                        double qty = qtyField.getValue() != null ? qtyField.getValue() : 0;
                        boolean customizable = customField.getValue();

                        return new Product.Ingredient(nameField.getValue(), qty, customizable);
                    })
                    .filter(ing -> ing != null)
                    .toList();

            current.setIngredients(ingredientsList);

            if (imageBuffer.getInputStream() != null) {
                try (InputStream is = imageBuffer.getInputStream();
                     ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    BufferedImage bufferedImage = ImageIO.read(is);
                    ImageIO.write(bufferedImage, "png", baos);
                    current.setImage(baos.toByteArray());
                }
            }

            service.save(current);
            Notification.show("Producto guardado");
            load();
            resetForm();
        } catch (ValidationException ex) {
            Notification.show("Revisa el formulario");
        } catch (Exception ex) {
            Notification.show("Error al guardar el producto");
        }
    }

    private void load() {
        grid.setItems(service.findAll());
    }

    private void resetForm() {
        current = new Product();
        binder.readBean(current);
        grid.deselectAll();
        ingredientsContainer.removeAll();
        previewImage.setSrc("");
        imageUpload.clearFileList();
    }

    private static String formatMoney(Double value) {
        if (value == null) return "";
        return String.format("%.2f €", value);
    }

    private static void stylePrimary(Button b) {
        b.getStyle().set("background", "#ff6a1a")
                .set("color", "white")
                .set("font-weight", "700")
                .set("border-radius", "10px");
    }

    private static void styleTertiary(Button b) {
        b.getStyle().set("background", "#f3f4f6")
                .set("color", "#111827")
                .set("border-radius", "10px");
    }

    private static void styleError(Button b) {
        b.getStyle().set("background", "#fee2e2")
                .set("color", "#991b1b")
                .set("border-radius", "10px");
    }
}
