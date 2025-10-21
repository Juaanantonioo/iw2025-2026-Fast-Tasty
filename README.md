# 🍔 FastFoodManager – Sistema de Gestión Integral para Locales de Comida Rápida

> Una aplicación web moderna para digitalizar la gestión de locales de comida rápida.  
> Proyecto desarrollado en el marco de la asignatura **Ingeniería Web (UCA · Curso 2025-2026)**.

---

## 🚀 Descripción

**FastFoodManager** es una aplicación web que permite a los pequeños locales de comida rápida gestionar de forma sencilla sus pedidos, menús, cobros y análisis de negocio.

El sistema está dividido en dos áreas:

- **Front-Office (Clientes):** carta digital interactiva, pedidos online y pago.  
- **Back-Office (Administradores):** gestión de pedidos, control de menús y visualización de métricas.

El objetivo del proyecto es **aplicar buenas prácticas de ingeniería web** utilizando **Java + Spring + Vaadin**, bajo una metodología ágil **SCRUM**.

---

## 🧱 Arquitectura

El sistema sigue un enfoque **MVC** y se compone de los siguientes módulos:

Cliente (Vaadin UI)
↓
Controladores REST (Spring MVC)
↓
Servicios y Repositorios (Spring Boot)
↓
Base de Datos MySQL / H2


---

## 🛠️ Tecnologías Utilizadas

| Componente | Tecnología               |
|:--|:-------------------------|
| Lenguaje principal | Java 17 / 21             |
| Framework backend | Spring Boot 3 / Spring MVC |
| Framework frontend | Vaadin 24+               |
| Base de datos | MySQL (prod) / H2 (local) |
| Dependencias | Apache Maven             |
| Control de versiones | Git + GitHub             |
| Entorno de desarrollo | Intellij                 |

---

## ⚙️ Instalación y Ejecución

### 🔧 Requisitos previos
- Java 17 o superior  
- Maven instalado  
- MySQL (opcional si usas H2 en local)

### 📦 Clonar el repositorio
```bash
git clone https://github.com/Juaanantonioo/iw2025-2026-Proyecto.git
cd iw2025-2026-Proyecto
