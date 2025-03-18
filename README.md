# 📖 CampusGo Wiki

Bienvenido a la Wiki de **CampusGo**, la aplicación diseñada para conectar estudiantes universitarios en la compra y venta de materiales académicos dentro de los campus.

## 📌 Índice

1. [Inicio](#inicio)
2. [Casos de Uso](#casos-de-uso)
3. [Estructura de la Aplicación](#estructura-de-la-aplicacion)
4. [Flujo de Navegación](#flujo-de-navegacion)
5. [Base de Datos](#base-de-datos)
6. [Guía de Desarrollo](#guia-de-desarrollo)
7. [API y Funcionalidades](#api-y-funcionalidades)

---

## 🔹 Inicio

CampusGo es una aplicación móvil que facilita la compra y venta de materiales de estudio dentro de los campus universitarios. La plataforma fomenta la economía colaborativa y permite a los estudiantes acceder a recursos académicos de manera eficiente y accesible.

### 🌟 Objetivos

- Facilitar la conexión entre estudiantes para la compra y venta de productos académicos.
- Ofrecer un sistema seguro de verificación universitaria.
- Permitir la comunicación en tiempo real entre usuarios.
- Integrar herramientas de seguimiento de entregas.

### 🌐 Características Principales

- **Creación de Perfiles**: Los usuarios pueden crear un perfil con su información universitaria y calificar a otros.
- **Verificación de Usuario**: Confirmación de identidad mediante escaneo de carné universitario.
- **Publicación de Productos**: Posibilidad de subir fotos y descripciones de materiales a vender.
- **Chat en Tiempo Real**: Mensajería entre compradores y vendedores.
- **Seguimiento de Pedidos**: Uso de GPS para rastrear ubicaciones de compradores y vendedores.
- **Modo Horizontal y Vertical**: Adaptabilidad de la interfaz según la orientación del dispositivo.

---

## 🔹 Casos de Uso

La aplicación maneja dos tipos de usuario: **compradores y vendedores**. Un mismo usuario puede desempeñar ambos roles simultáneamente.

### 🛍️ Compradores

- Explorar categorías de productos.
- Ver detalles de productos.
- Agregar productos al carrito y realizar pedidos.
- Comunicarse con vendedores mediante chat.
- Confirmar la entrega de productos.

### 🏷️ Vendedores

- Publicar productos con fotos y descripciones.
- Gestionar pedidos recibidos.
- Chatear con compradores interesados.
- Confirmar la entrega exitosa de productos.

---

## 🔹 Estructura de la Aplicación

La aplicación está compuesta por las siguientes actividades y vistas:

- **MainActivity (SplashScreen)** → Pantalla de bienvenida con el logo de CampusGo.
- **Inicio de Sesión / Registro** → Pantalla para iniciar sesión o registrarse.
- **HomeActivity** → Menú principal con opciones para Comprar o Vender.
- **Categorías** → Lista de categorías de productos.
- **Creación de Producto** → Formulario para publicar un producto.
- **Lista de Productos** → Vista de productos dentro de una categoría.
- **Detalle de Producto** → Información detallada con opciones de compra.
- **Mensajería** → Lista de chats organizados en pestañas (Clientes y Vendedores).
- **Chat** → Conversación con un usuario específico.
- **Carrito de Compras** → Productos agregados con opción de pago.
- **Seguimiento de Pedido** → Ubicación en tiempo real del producto comprado.

---

## 🔹 Flujo de Navegación

### 🔄 Proceso de Compra

1. El usuario inicia sesión y accede a la opción **Comprar**.
2. Selecciona una categoría y explora los productos disponibles.
3. Elige un producto y lo agrega al carrito.
4. Procede al pedido y elige un método de pago.
5. Se comunica con el vendedor y coordina la entrega.
6. Confirma la entrega del producto.

### 📦 Proceso de Venta

1. El usuario inicia sesión y accede a la opción **Vender**.
2. Publica un nuevo producto con fotos y detalles.
3. Recibe mensajes de compradores interesados.
4. Gestiona el estado de los pedidos y realiza la entrega.
5. Confirma la entrega exitosa del producto.

---

## 🔹 Base de Datos

La base de datos está estructurada en las siguientes entidades:

### **Usuarios**
- `id` (String)
- `nombre` (String)
- `universidad` (String)
- `fotoPerfilUrl` (String)

### **Productos**
- `id` (String)
- `vendedorId` (String)
- `categoria` (String)
- `nombre` (String)
- `descripcion` (String)
- `precio` (Double)
- `imagenUrl` (String)
- `estado` (Disponible / Vendido)

### **Chats**
- `id` (String)
- `usuario1` (String)
- `usuario2` (String)
- `mensajes` (Lista de Mensajes)

### **Pedidos**
- `id` (String)
- `compradorId` (String)
- `vendedorId` (String)
- `productos` (Lista de Productos)
- `estado` (Pendiente / Entregado)
- `fecha` (Long)

### **Métodos de Pago (Solo elección, sin pasarela de pagos)**
- `id` (String)
- `usuarioId` (String)
- `tipo` (Efectivo / Transferencia / Nequi / Otro)

---

## 🔹 API y Funcionalidades

- **Autenticación de usuarios** mediante correo institucional.
- **Publicación y gestión de productos** con imágenes.
- **Chat en tiempo real** para compradores y vendedores.
- **Confirmación de entrega** sin pasarela de pagos.
- **Seguimiento de pedidos** a través de la aplicación.

---

## 👥 Equipo de Desarrollo

- **Carlos Mercado**
- **Sara Albarracín**
- **Victoria Roa**
- **Alejandro Caicedo**

