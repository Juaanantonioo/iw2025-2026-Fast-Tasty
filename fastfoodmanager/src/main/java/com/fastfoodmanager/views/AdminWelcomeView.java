package com.fastfoodmanager.views;

import com.fastfoodmanager.domain.WelcomeSettings;
import com.fastfoodmanager.service.WelcomeSettingsService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Route(value = "admin/welcome", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class AdminWelcomeView extends VerticalLayout {

    private final WelcomeSettingsService welcomeSettingsService;

    private TextField siteTitle = new TextField("Título principal");
    private TextField siteSubtitle = new TextField("Subtítulo");
    private TextField address = new TextField("Dirección");
    private TextField googleMapsUrl = new TextField("URL de Google Maps");
    private TextArea scheduleText = new TextArea("Horarios (texto)");

    private MemoryBuffer buffer = new MemoryBuffer();
    private Upload upload = new Upload(buffer);

    private VerticalLayout imagesLayout = new VerticalLayout();

    public AdminWelcomeView(WelcomeSettingsService welcomeSettingsService) {
        this.welcomeSettingsService = welcomeSettingsService;

        setPadding(true);
        setSpacing(true);

        WelcomeSettings settings = welcomeSettingsService.get();

        siteTitle.setValue(settings.getSiteTitle() != null ? settings.getSiteTitle() : "");
        siteSubtitle.setValue(settings.getSiteSubtitle() != null ? settings.getSiteSubtitle() : "");
        TextField siteDomain = new TextField("Dominio visible");
        siteDomain.setValue(settings.getSiteDomain() != null ? settings.getSiteDomain() : "");
        address.setValue(settings.getAddress() != null ? settings.getAddress() : "");
        googleMapsUrl.setValue(settings.getGoogleMapsUrl() != null ? settings.getGoogleMapsUrl() : "");
        scheduleText.setValue(settings.getScheduleText() != null ? settings.getScheduleText() : "");
        scheduleText.setWidthFull();
        scheduleText.setHeight("150px");

        upload.setAcceptedFileTypes("image/*");
        upload.addSucceededListener(e -> {
            try {
                byte[] bytes = buffer.getInputStream().readAllBytes();
                List<byte[]> imgs = new ArrayList<>(settings.getCarouselImages());
                imgs.add(bytes);
                settings.setCarouselImages(imgs);
                welcomeSettingsService.save(settings);
                Notification.show("Imagen añadida al carrusel");
                refreshImages(settings);
            } catch (Exception ex) {
                Notification.show("Error al subir la imagen");
            }
        });

        Button save = new Button("Guardar texto", ev -> {
            settings.setSiteTitle(siteTitle.getValue());
            settings.setSiteSubtitle(siteSubtitle.getValue());
            settings.setSiteDomain(siteDomain.getValue());
            settings.setAddress(address.getValue());
            settings.setGoogleMapsUrl(googleMapsUrl.getValue());
            settings.setScheduleText(scheduleText.getValue());
            welcomeSettingsService.save(settings);
            Notification.show("Configuración guardada");
        });

        H2 title = new H2("Configuración de la página de bienvenida");
        H3 textTitle = new H3("Textos principales");
        H3 imagesTitle = new H3("Imágenes del carrusel");

        refreshImages(settings);

        add(
                new H2("Configuración de la página de bienvenida"),
                siteDomain,
                title,
                textTitle,
                siteTitle,
                siteSubtitle,
                address,
                googleMapsUrl,
                scheduleText,
                save,
                imagesTitle,
                upload,
                imagesLayout
        );
    }

    private void refreshImages(WelcomeSettings settings) {
        imagesLayout.removeAll();

        List<byte[]> imgs = settings.getCarouselImages();
        if (imgs == null || imgs.isEmpty()) return;

        for (int i = 0; i < imgs.size(); i++) {
            int index = i;
            byte[] data = imgs.get(i);

            HorizontalLayout row = new HorizontalLayout();
            row.setAlignItems(Alignment.CENTER);

            Image img = new Image();
            img.setWidth("120px");
            img.setSrc("data:image/png;base64," +
                    Base64.getEncoder().encodeToString(data));

            Button delete = new Button("Eliminar", e -> {
                List<byte[]> list = new ArrayList<>(settings.getCarouselImages());
                list.remove(index);
                settings.setCarouselImages(list);
                welcomeSettingsService.save(settings);
                Notification.show("Imagen eliminada");
                refreshImages(settings);
            });

            row.add(img, delete);
            imagesLayout.add(row);
        }
    }
}
