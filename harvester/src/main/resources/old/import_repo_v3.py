'''
Desenvolvido por:
- Bruno
- Leandro
'''

import sickle, requests, csv, ast, time



def coletar_dados_oai_pmh(url_base, 
                          conjunto=None,                            
                          ano_inicial=None,
                          metadados='oai_dc'):
    """Coleta registros de um provedor OAI-PMH.

    Args:
        url_base (str): URL base do provedor OAI-PMH.
        conjunto (str, optional): Nome do conjunto de dados a ser coletado (se aplicável).
        metadados (str, optional): Formato de metadados desejado (padrão: 'oai_dc').
        ano_inicial (str, opcional): ano-mes-dia inicial da coleta

    Yields:
        dict: Dicionário contendo os metadados do registro.
    """

    client = sickle.Sickle(url_base)

    parametros = {
        'metadataPrefix': metadados,        
    }

    if conjunto:
        parametros['set'] = conjunto

    if ano_inicial:
        parametros['from'] = ano_inicial
    
    records = client.ListRecords(**parametros)

    for record in records:
        yield record.metadata  # Retorna os metadados como um dicionário

def carregar_csv(nome_arquivo):
    """
    Carrega dados de um arquivo CSV para um dicionário.

    Args:
        nome_arquivo: O nome do arquivo CSV a ser lido.

    Returns:
        Uma lista de dicionários, onde cada dicionário representa uma linha do CSV.
    """

    dados = []

    with open(nome_arquivo, 'r', newline='', encoding='utf-8') as arquivo_csv:
        leitor_csv = csv.DictReader(arquivo_csv)
        for linha in leitor_csv:
            dados.append(linha)

    return dados

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

# Programa Principal

server_url = "https://lume.ufrgs.br/bitstream/" 
nome_do_arquivo_csv = "ufrgs_col_10183_43_2021-01-01.csv"
dados_do_csv = carregar_csv(nome_do_arquivo_csv)
bases = {
///"Biblioteca Digital de Teses Brasileiras", "https://bdtd.ibict.br/vufind/OAI/"
    'usp': {
        'servidor': 'https://bdtd.ibict.br/vufind/oai',
        'conjuntos': [''],
        'processar': False,
        },
    'unesp': {
        'servidor': 'http://repositorio.unesp.br/oai/',
        'conjuntos': ['col_unesp'],
        'processar': False,
        },
    'ufrgs': {
        'servidor': 'http://lume.ufrgs.br/oai',
        'conjuntos': ['col_10183_26428','col_10183_43'],
        'processar': True,
        },
    'ibict': {
        'servidor': 'https://bdtd.ibict.br/vufind/oai',
        'conjuntos': [''],
        'processar': False,
        },
    'oasis': {
        'servidor': 'https://oasisbr.ibict.br/vufind/',
        'conjuntos': [''],
        'processar': False,
        },              # https://www.lareferencia.info/pt/                  # https://dadosabertos.capes.gov.br/group/catalogo-de-teses-e-dissertacoes-brasil
    }

for base in bases:
    if bases[base]['processar']:
        url_base = bases[base]['servidor']
        conjuntos = bases[base]['conjuntos']
        ano = "2020-01-01" 

        print(f"Contatando servidor {base} ({url_base})...")
        
        for conjunto in conjuntos:        
            arquivo_csv = f"{base}_{conjunto}_{ano}.csv" 
            print(f"\tProcessando conjunto {conjunto} e recuperando dados a partir de {ano}...") 
            registros = 0
            with open(arquivo_csv, 'w', newline='', encoding='utf-8') as csvfile:
                campos = None
                writer = None    
                for metadados in coletar_dados_oai_pmh(url_base+'/request', conjunto, ano):
                    if campos is None:
                        campos = metadados.keys()
                        writer = csv.DictWriter(csvfile, fieldnames=campos)
                        writer.writeheader()                
                    registros+=1
                    writer.writerow(metadados)

            print(f"\t{registros} registros encontrados e salvos no arquivo {arquivo_csv}.") 