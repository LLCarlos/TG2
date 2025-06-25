import requests, csv, ast, time, pandas

def baixar_pdf(url, nome_arquivo):
    """
    Baixa um PDF de uma URL e salva em um arquivo local.

    Args:
        url: A URL do PDF.
        nome_arquivo: O nome do arquivo local onde o PDF será salvo.
    """

    resposta = requests.get(url)
    resposta.raise_for_status()  # Lança uma exceção se houver um erro na requisição

    with open(nome_arquivo, 'wb') as arquivo:
        arquivo.write(resposta.content)

    print(f"PDF baixado e salvo em: {nome_arquivo}")

# Programa principal

arquivo = "Resultados.xlsx"
dados = pandas.read_excel(arquivo)

print(dados)

for recurso in dados_do_csv:
    identificador = ast.literal_eval(recurso['identifier'])

    pdf_sub_url = f"{identificador[0].removeprefix("http://hdl.handle.net")}/handle/{identificador[1]}"
    url_sufix = ".pdf?sequence=1&isAllowed=y"
    full_url = f"{server_url}{pdf_sub_url}{url_sufix}"

    print(f"Baixando PDF: {full_url}")

    nome_do_arquivo = f"{dados_do_csv[0]['type']}".removeprefix("['").removesuffix("']")
    nome_do_arquivo = f"{ast.literal_eval(dados_do_csv[0]['date'])[1]}_{nome_do_arquivo}_{identificador[1]}.pdf"

    baixar_pdf(full_url, nome_do_arquivo)

    time.sleep(1)
