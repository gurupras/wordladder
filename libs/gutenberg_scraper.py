#!/usr/bin/python

def get_top100_urls():
    import mechanize
    import re
    base_txt_url = "http://www.gutenberg.org/files/"
    urls = []
    br = mechanize.Browser()
    br.open("http://www.gutenberg.org/browse/scores/top")
    links = br.links()
    for link in links:
        #print link.url
        match = re.match("/ebooks/(\\d+)", link.url)
        if match:
            book_id = match.group(1)
            urls.append(base_txt_url + book_id + "/" + book_id + ".txt")
    return urls

def store_pickle(freq_dict, output=None):
    import pickle
    import datetime
    if output is None:
        date = datetime.datetime.now()
        date = datetime.datetime(date.year, date.month, date.day)
        output=date.strftime("%Y-%m-%d") + ".p"
    with open(output, "wb") as output_file: 
        pickle.dump(freq_dict, output_file, 2)

def load_pickle(input_file):
    import pickle
    with open(input_file, "rb") as f:
        return pickle.load(f)

def parse_book(link, freq_dict=None):
    import mechanize
    import re
    br = mechanize.Browser()
    book = br.open(link)
    global_freq_dict = freq_dict
    freq_dict = {}
    start_flag = False
    for line in book:
        # Check for start pattern
        if re.match("\\*\\*\\* START OF THIS PROJECT GUTENBERG EBOOK.*\\*\\*\\*", line):
            #print 'Reached start on line :' + line
            start_flag = True
        # Check for end pattern
        if re.match("\\*\\*\\* END OF THIS PROJECT GUTENBERG EBOOK.*\\*\\*\\*", line):
            #print 'Reached end on line :' + line
            break
        if start_flag is False:
            continue
        word_list = re.sub("[^\w]", " ", line).split()
            
        for word in word_list:
            if freq_dict.get(word) is None:
                freq_dict.update({word : 0})
            if global_freq_dict.get(word) is None:
                global_freq_dict.update({word : 0})
            freq_dict.update({word : freq_dict.get(word) + 1})
            global_freq_dict.update({word : global_freq_dict.get(word) + 1})
    return freq_dict

if __name__ == "__main__":
    import time
    links = get_top100_urls()
    freq_dict = {}
    idx = 0
    for link in links:
        print 'Finished book ' + str(idx + 1)
        #print link
        parse_book(links[0], freq_dict)
        time.sleep(300)
        idx += 1
    #for key in freq:
    #    print key + " " + str(freq.get(key))
    store_pickle(freq_dict)

