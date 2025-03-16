# 📖 CampusGo Wiki

Bienvenido a la **Wiki** de *CampusGo*, la aplicación diseñada para conectar estudiantes universitarios en la compra y venta de materiales académicos dentro de los campus.

---

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

**CampusGo** es una aplicación móvil que facilita la compra y venta de materiales de estudio dentro de los campus universitarios. La plataforma fomenta la economía colaborativa y permite a los estudiantes acceder a recursos académicos de manera eficiente y accesible.

### 🌟 Objetivos
- Facilitar la conexión entre estudiantes para compra y venta de productos académicos.
- Ofrecer un sistema seguro de verificación universitaria.
- Permitir la comunicación en tiempo real entre usuarios.
- Integrar herramientas de seguimiento de entregas.

---

## 🔹 Casos de Uso

La aplicación maneja dos tipos de usuario: **compradores** y **vendedores**. Un mismo usuario puede desempeñar ambos roles simultáneamente.

### 🛍️ Compradores
- Explorar categorías de productos.
- Ver detalles de productos.
- Agregar productos al carrito y realizar compras.
- Comunicarse con vendedores mediante chat.
- Realizar pagos y seguir pedidos en tiempo real.

### 🏷️ Vendedores
- Publicar productos con fotos y descripciones.
- Gestionar pedidos recibidos.
- Chatear con compradores interesados.
- Actualizar estado de productos vendidos.
- Configurar métodos de pago y entrega.

---

## 🔹 Estructura de la Aplicación

La aplicación está compuesta por las siguientes actividades y vistas:

1. **MainActivity (SplashScreen)** → Pantalla de bienvenida con el logo de *CampusGo*.
2. **Inicio de Sesión / Registro** → Pantalla para iniciar sesión o registrarse.
3. **HomeActivity** → Menú principal con opciones para *Comprar* o *Vender*.
4. **Categorías** → Lista de categorías de productos.
5. **Creación de Producto** → Formulario para publicar un producto.
6. **Lista de Productos** → Vista de productos dentro de una categoría.
7. **Detalle de Producto** → Información detallada con opciones de compra.
8. **Mensajería** → Lista de chats organizados en pestañas (*Clientes* y *Vendedores*).
9. **Chat** → Conversación con un usuario específico.
10. **Carrito de Compras** → Productos agregados con opción de pago.
11. **Métodos de Pago** → Selección de pago y confirmación.
12. **Seguimiento de Pedido** → Ubicación en tiempo real del producto comprado.

---

## 🔹 Flujo de Navegación

### 🔄 Proceso de Compra
1. El usuario inicia sesión y accede a la opción *Comprar*.
2. Selecciona una categoría y explora los productos disponibles.
3. Elige un producto y lo agrega al carrito.
4. Procede al pago y confirma la compra.
5. Contacta al vendedor si es necesario y sigue el pedido.

### 📦 Proceso de Venta
1. El usuario inicia sesión y accede a la opción *Vender*.
2. Publica un nuevo producto con fotos y detalles.
3. Recibe mensajes de compradores interesados.
4. Gestiona el estado de los pedidos y confirma entregas.

---

## 🔹 Base de Datos

La base de datos está estructurada en las siguientes entidades:

- **Usuarios** (ID, Nombre, Universidad, Rol, Foto de perfil)
- **Productos** (ID, Vendedor, Categoría, Precio, Descripción, Estado)
- **Chats** (ID, Usuario1, Usuario2, Mensajes)
- **Pedidos** (ID, Comprador, Vendedor, Productos, Estado, Pago)
- **Métodos de Pago** (ID, Usuario, Tipo, Información de Tarjeta o Cuenta)

---

## 🔹 Guía de Desarrollo

### 🔧 Instalación
Para configurar el entorno de desarrollo en **Android Studio**:

1. Clonar el repositorio:
   ```bash
   git clone https://github.com/tu-repo/campusgo.git
