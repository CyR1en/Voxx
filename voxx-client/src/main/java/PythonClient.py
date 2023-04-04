import tkinter as tk
import asyncio
import websockets

# Define the WebSocket server URL and port
SERVER_URL = "ws://localhost:8000/ws"


async def send_message(websocket, message):
    """
    Sends a message to the WebSocket server.
    """
    await websocket.send("PY|" + websocket.username + "|" + message).encode("utf-8"))


async def receive_message(websocket):
    """
    Receives a message from the WebSocket server.
    """
    message = await websocket.recv()
    return message


async def main():
    # Connect to the WebSocket server
    async with websockets.connect(SERVER_URL) as websocket:
        # Send a message to the JavaFX client
        await send_message((websocket,  "PY|" + websocket.username + "|" + message).encode("utf-8"))

        # Receive a message from the JavaFX client
        response = await receive_message(websocket)
        print(response)

        # Send a message to the Java server
        await send_message(websocket, message)

        # Receive a message from the Java server
        response = await receive_message(websocket)
        print(response)


if __name__ == "__main__":
    asyncio.run(main())

# Create GUI
root = tk.Tk()
chat_text = tk.Text(root, state='disabled')
chat_text.pack()
input_field = tk.Entry(root)
input_field.pack()
send_button = tk.Button(root, text='Send', command=lambda: send_message())
send_button.pack()

# Start GUI
root.mainloop()
