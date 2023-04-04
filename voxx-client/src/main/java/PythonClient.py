import socket
import threading
import tkinter as tk

# Initialize host and port
HOST = 'localhost'
PORT = 1027

# Create socket object
py_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

# Bind socket to the address
py_socket.bind((HOST, PORT))

# Listen for incoming connections
py_socket.listen()

# Create an empty list to hold client threads
threads = []


# Define a function to handle each new client connection
def handle_client(client_socket, address):
    while True:
        # Receive message from client
        message = client_socket.recv(1024).decode()

        # Send message to all clients
        for thread in threads:
            if thread != threading.current_thread():
                thread.client_socket.send(f'{address[0]}: {message}'.encode())


# Define a function to accept incoming connections and start a new thread for each client

def accept_connections():
    while True:
        # Accept incoming connection
        client_socket, address = py_socket.accept()

        # Create a new thread for the client and start it
        thread = threading.Thread(target=handle_client, args=(client_socket, address))
        thread.client_socket = client_socket
        thread.start()

        # Add the new thread to the list of client threads
        threads.append(thread)


# Create GUI
root = tk.Tk()
chat_text = tk.Text(root, state='disabled')
chat_text.pack()
input_field = tk.Entry(root)
input_field.pack()
send_button = tk.Button(root, text='Send', command=lambda: send_message())
send_button.pack()


# Define message sending function
def send_message():
    message = input_field.get()
    for thread in threads:
        thread.client_socket.send(f'{HOST}: {message}'.encode())
    input_field.delete(0, tk.END)


# Create and start thread for accepting connections
accept_thread = threading.Thread(target=accept_connections)
accept_thread.start()

# Start GUI
root.mainloop()
