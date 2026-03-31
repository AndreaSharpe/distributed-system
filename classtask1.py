import numpy as np
from hmmlearn.hmm import CategoricalHMM

# 状态和观测
states = ["晴天", "雨天"]        # S1, S2
n_states = len(states)

observations = ["开心", "低落"]  # O1, O2
n_observations = len(observations)

# 映射观测为索引
obs_map = {"开心": 0, "低落": 1}
obs_sequence = [obs_map[o] for o in ["低落", "开心", "开心", "开心", "低落"]]

# 状态转移矩阵 A
A = np.array([
    [0.8, 0.2],  # S1 -> S1,S2
    [0.4, 0.6]   # S2 -> S1,S2
])

# 发射矩阵 B
B = np.array([
    [0.7, 0.3],  # S1 -> O1,O2
    [0.4, 0.6]   # S2 -> O1,O2
])

pi = [2/3,1/3]

model = CategoricalHMM(n_components=n_states, init_params="")
model.startprob_ = pi
model.transmat_ = A
model.emissionprob_ = B

obs_seq_array = np.array(obs_sequence).reshape(-1, 1)

logprob = model.score(obs_seq_array)
prob = np.exp(logprob)
print("观测序列 (O2, O1, O1, O1, O2) 的概率:", prob)