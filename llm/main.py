from langchain_ollama.llms import OllamaLLM
from langchain_core.prompts import ChatPromptTemplate
from vector import retriever
import time
# Initialize the model
model = OllamaLLM(model='llama3.2')

template = """
Você é um assistente de pesquisa da universidade, especializado em ajudar alunos a encontrar teses e dissertações relevantes.
Sua principal função é analisar a pergunta do aluno e uma lista de teses e dissertações recuperadas, e então apresentar os resultados mais pertinentes de forma clara e informativa.

**Instruções Detalhadas para sua Resposta:**

1.  **Análise Cuidadosa:** Examine cada uma das teses/dissertações fornecidas no contexto da pergunta do aluno: "{question}".
2.  **Formato da Resposta para Teses Relevantes:** Para cada tese/dissertação que você julgar altamente relevante, apresente as seguintes informações:
    * **Título:** (Extraia e apresente o título da tese/dissertação)
    * **Ano de Publicação:** (Extraia dos metadados ou do conteúdo, se disponível)
    * **Área de Conhecimento:** (Extraia da tese/dissertação)
    * **Palavras-chave/Assuntos:** (Liste as palavras-chave ou assuntos principais)
    * **Breve Justificativa da Relevância:** Em 1-2 frases, explique por que esta tese/dissertação é relevante para a pergunta do aluno. Baseie-se no título, resumo (se disponível no contexto fornecido) ou palavras-chave.
    * **Link para Acesso:** (Se um link de acesso estiver disponível nos metadados, inclua-o)
3.  **Priorização:** Apresente no máximo as 3 (três) teses/dissertações mais relevantes. Se houver outras teses nos exemplos que também pareçam relevantes, você pode mencioná-las brevemente ao final, sem detalhamento completo.
4.  **Caso de Não Relevância:** Se, após analisar todos os exemplos fornecidos, você concluir que nenhuma das teses/dissertações se relaciona diretamente com a pergunta do aluno, informe educadamente: "Com base nos documentos fornecidos, não encontrei informações diretamente relacionadas à sua pergunta. Você poderia tentar reformular sua busca?"
5.  **Tom e Estilo:** Mantenha um tom formal, acadêmico e prestativo. Utilize negrito para destacar os campos (ex: **Título:**).
6. Não alucine informações. Baseie suas respostas estritamente nos dados fornecidos no contexto e nos metadados das teses/dissertações.

**Pergunta do Aluno:**
{question}

**Sua Resposta Detalhada:**
"""
prompt = ChatPromptTemplate.from_template(template)
chain = prompt | model

def main():
    """Main function to handle user interaction."""
    print("Bem-vindo ao sistema de pesquisa de teses e dissertações!")
    print("Digite 'exit' para sair.")

    while True:
        try:
            question = input("\nDigite sua pergunta: ")
            if question.strip().lower() == 'exit':
                print("Encerrando o sistema. Até logo!")
                break


            # Retrieve relevant theses
            retrieved_docs = retriever.invoke(question) # Lista de objetos Document

            # Formatar os documentos recuperados para o prompt
            formatted_thesis_context = ""
            if not retrieved_docs:
                formatted_thesis_context = "Nenhuma tese foi encontrada pelo sistema de busca para esta pergunta."
            else:
                for i, doc in enumerate(retrieved_docs):
                    # doc.page_content deve ter: Título, Área de conhecimento, Palavras-chave, Resumo
                    # doc.metadata deve ter: ano_publicacao, tipo_documento, link_acesso, area, titulo_original
                    
                    formatted_thesis_context += f"--- Exemplo de Tese/Dissertação {i+1} ---\n"
                    formatted_thesis_context += f"{doc.page_content}\n"
                    
                    # Adiciona metadados relevantes explicitamente para o LLM ver
                    if doc.metadata:
                        formatted_thesis_context += f"Ano de Publicação (Metadados): {doc.metadata.get('ano_publicacao', 'Não informado')}\n"
                        formatted_thesis_context += f"Tipo de Documento (Metadados): {doc.metadata.get('tipo_documento', 'Não informado')}\n"
                        if doc.metadata.get('link_acesso'):
                            formatted_thesis_context += f"Link para Acesso (Metadados): {doc.metadata.get('link_acesso')}\n"
                    formatted_thesis_context += "---------------\n\n"

            chain_input = {
                "thesis": formatted_thesis_context,
                "question": question,
            }
            
            print("\nChain Input:")
            print(chain_input)


            start_time = time.time()
            result = chain.invoke(chain_input)
            end_time = time.time()

            print("\n\n\nResposta:\n\n\n\n")
            print(result)
            print(f"Tempo para responder: {end_time - start_time:.2f} segundos")

            #save the question and result to a file 
            with open("respostas.txt", "a", encoding="utf-8") as file:
                file.write(f"Pergunta: {question}\n")
                file.write(f"Resposta: {result}\n")
                file.write(f"Tempo para responder: {end_time - start_time:.2f} segundos\n")
                file.write("-" * 50 + "\n")
        except KeyboardInterrupt:
            print("\nEncerrando o sistema. Até logo!")
            break

        except Exception as e:
            print(f"Ocorreu um erro: {e}")

if __name__ == "__main__":
    main()
