msg = "FP|F6|R1|L1|L0|!"
data = msg.split(sep="|", maxsplit=1)
command = data[0]
payload = data[1]

dir_move_map = {'F': 'F', 'R': 'R', 'L': 'L', 'B': 'B'} #TODO -- to be confirmed

if payload[-1] == "!":
    payload = payload[:-2] #remove |!
    print(payload)

path_seq = payload.split("|")
print(path_seq)

step_seq = []

for step in path_seq:
    if (step == "L1"):
        step_seq = step_seq + list(dir_move_map['L'] + dir_move_map['F'])
    elif (step == "R1"):
        step_seq = step_seq + list(dir_move_map['R'] + dir_move_map['F'])
    elif (step == "B1"):
        step_seq = step_seq + list(dir_move_map['R'] + dir_move_map['R'] + dir_move_map['F'])
    elif (step == "L0"):
        step_seq = step_seq + list(dir_move_map['L'])
    elif (step == "R0"):
        step_seq = step_seq + list(dir_move_map['R'])
    else:
        next_step = list(str(step))
        dir = next_step[0]
        num_steps = int(next_step[1])
        
        step_seq = step_seq + [dir_move_map[next_step[0]] for x in range(int(next_step[1]))]
        
print("step seq: ", step_seq)
print('FP|{steps}'.format(steps=','.join(step_seq)))



def compressStepSeq1(step_seq):
    forward_count = 0
    seq = []
    for step in step_seq:
        if step == 'F':
            forward_count = forward_count + 1
        else:
            seq.append('F{x}'.format(x=forward_count))
            seq.append(step)
            forward_count = 0
    if forward_count > 0:
        seq.append('F{x}'.format(x=forward_count))
    return seq

print("step_seq from func: ", compressStepSeq1(step_seq))
print('FP|{steps}'.format(steps=','.join(compressStepSeq1(step_seq))))

# msg = "MDF|FFC07F80FF01FE03FFFFFFF3FFE7FFCFFF9C7F38FE71FCE3F87FF0FFE1FFC3FF87FF0E0E1C1F|FFC07F80FF01FE03FFFFFFF3FFE7FFCFFF9C7F38FE71FCE3F87FF0FFE1FFC3FF87FF0E0E1C1F|!"

# data = msg.split(sep='|', maxsplit=1)
# command = data[0]
# payload = data[1]


# print(command + "\n" + payload)