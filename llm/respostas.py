def main():
    total_time = 0.0
    count = 0
    time_entry = []
    highest_time = 0.0
    lowest_time = 50.0
    median = 0.0
    search_str = "Tempo para responder: "
    with open("respostas.txt", "r", encoding="utf-8") as f:
        for line in f:
            if search_str in line:
                try:
                    part = line.split(search_str)[1]
                    seconds_str = part.split(" segundos")[0].strip()
                    seconds = float(seconds_str)
                    time_entry.append(seconds)
                    # Accumulate total time and track highest/lowest times
                    total_time += seconds
                    if seconds > highest_time:
                        highest_time = seconds
                    if seconds < lowest_time:
                        lowest_time = seconds
                    count += 1
                except (IndexError, ValueError):
                    continue
    if count > 0:
        avg_time = total_time / count
        print(f"Média por entrada: {avg_time:.2f} segundos")
        print(f"Maior tempo: {highest_time:.2f} segundos")
        print(f"Menor tempo: {lowest_time:.2f} segundos")
        #calculate median
        time_entry.sort()
        if count % 2 == 0:
            median = (time_entry[count // 2 - 1] + time_entry[count // 2]) / 2
        else:
            median = time_entry[count // 2]  
        #save to a file
        with open("estatisticas.txt", "w", encoding="utf-8") as stats_file:
            stats_file.write(f"Média por entrada: {avg_time:.2f} segundos\n")
            stats_file.write(f"Maior tempo: {highest_time:.2f} segundos\n")
            stats_file.write(f"Menor tempo: {lowest_time:.2f} segundos\n")
            stats_file.write(f"Total de entradas processadas: {count}\n")
            stats_file.write(f"Todas entradas: {time_entry}\n")
    else:
        print("Nenhuma entrada encontrada.")

if __name__ == "__main__":
    main()