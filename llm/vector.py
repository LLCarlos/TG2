from langchain_ollama import OllamaEmbeddings
from langchain_chroma import Chroma
from langchain_core.documents import Document
import os
import pandas as pd
import time

df = pd.read_csv("results/GERAL/ufrgs.csv", sep="|")
embeddings = OllamaEmbeddings(model="mxbai-embed-large")
#Print the DF number os rows
print(f"Number of rows in the dataframe: {len(df)}")

db_location = "./chrome_langchain_db"
add_documents = not os.path.exists(db_location)
print("Processing documents...")
#Create a start time variable to measure the time it took to process the documents
start_time = time.time()
documents = []

if add_documents:
    ids = []
    
    for i, row in df.iterrows():
                # Garantir que os campos sejam strings e tratar valores ausentes
        titulo = str(row.get('Título', ''))
        area = str(row.get('Área', '')) # Supondo que 'Área' exista no CSV
        assuntos = str(row.get('Assuntos', ''))
        descricao = str(row.get('Descrição', ''))
        document = Document(
        page_content = (
            f"Título: {titulo}\n"
            f"Área de conhecimento: {area}\n"
            f"Palavras-chave: {assuntos}\n"
            f"Resumo: {descricao}"
        ),            
        metadata={
            "data": row["Ano de Publicação"],
            # "Tipo de Documento": row.get("Tipo de Documento"),kKk
            "Link de Acesso": row.get("Link de Acesso"),
            },
        )
        ids.append(str(i))
        documents.append(document)        
#print the time it took to process the documents
print(f"Processed {len(documents)} documents in {time.time() - start_time:.2f} seconds")
print("Creating vector store...")
start_time = time.time()
vector_store = Chroma(
    collection_name="ufrgs",
    persist_directory=db_location,
    embedding_function=embeddings
)
print(f"Created vector store in {time.time() - start_time:.2f} seconds")
#Persist the vector store to disk
print(f"adding {len(documents)} documents to vector store...")
start_time = time.time()
if add_documents:
    vector_store.add_documents(documents=documents, ids=ids)
print(f"Persisted {len(documents)} documents to disk in {time.time() - start_time:.2f} seconds")
retriever = vector_store.as_retriever(
    search_kwargs={"k": 10}
)