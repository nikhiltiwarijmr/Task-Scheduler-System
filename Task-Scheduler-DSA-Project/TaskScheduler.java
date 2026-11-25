import java.util.Scanner;

public class TaskScheduler {

    // 1. Task Model
    static class Task {
        String id;
        String name;
        int priority;
        int sequence; // Maintained for FIFO arrival ordering on priority ties

        public Task(String id, String name, int priority, int sequence) {
            this.id = id;
            this.name = name;
            this.priority = priority;
            this.sequence = sequence;
        }
    }

    // 2. Custom Min-Heap Implementation (Priority 1 = Highest Priority)
    static class MinHeap {
        private Task[] heap;
        private int size;
        private int capacity;

        public MinHeap(int initialCapacity) {
            this.capacity = initialCapacity;
            this.size = 0;
            this.heap = new Task[capacity];
        }

        private void ensureCapacity() {
            if (size >= capacity) {
                capacity *= 2;
                Task[] newHeap = new Task[capacity];
                System.arraycopy(heap, 0, newHeap, 0, size);
                heap = newHeap;
            }
        }

        private boolean isHigherPriority(Task a, Task b) {
            if (a.priority != b.priority) {
                return a.priority < b.priority;
            }
            return a.sequence < b.sequence;
        }

        public void insert(Task task) {
            ensureCapacity();
            heap[size] = task;
            int current = size;
            size++;

            while (current > 0) {
                int parent = (current - 1) / 2;
                if (isHigherPriority(heap[current], heap[parent])) {
                    Task temp = heap[current];
                    heap[current] = heap[parent];
                    heap[parent] = temp;
                    current = parent;
                } else {
                    break;
                }
            }
        }

        public Task extractMin() {
            if (size == 0) return null;

            Task minTask = heap[0];
            heap[0] = heap[size - 1];
            heap[size - 1] = null;
            size--;

            int current = 0;
            while (current < size) {
                int left = 2 * current + 1;
                int right = 2 * current + 2;
                int smallest = current;

                if (left < size && isHigherPriority(heap[left], heap[smallest])) {
                    smallest = left;
                }
                if (right < size && isHigherPriority(heap[right], heap[smallest])) {
                    smallest = right;
                }

                if (smallest != current) {
                    Task temp = heap[current];
                    heap[current] = heap[smallest];
                    heap[smallest] = temp;
                    current = smallest;
                } else {
                    break;
                }
            }

            return minTask;
        }

        public boolean isEmpty() {
            return size == 0;
        }

        public Task[] getCopy() {
            Task[] copy = new Task[size];
            System.arraycopy(heap, 0, copy, 0, size);
            return copy;
        }
    }

    // 3. Custom FIFO Queue for Executed/Completed Tasks
    static class QueueNode {
        Task task;
        QueueNode next;

        public QueueNode(Task task) {
            this.task = task;
            this.next = null;
        }
    }

    static class CustomQueue {
        private QueueNode front;
        private QueueNode rear;

        public void enqueue(Task task) {
            QueueNode newNode = new QueueNode(task);
            if (rear == null) {
                front = rear = newNode;
            } else {
                rear.next = newNode;
                rear = newNode;
            }
        }

        public boolean isEmpty() {
            return front == null;
        }

        public void displayCompleted() {
            if (isEmpty()) {
                System.out.println("Completed tasks: None");
                return;
            }

            System.out.print("Completed: ");
            QueueNode current = front;
            boolean first = true;
            while (current != null) {
                if (!first) {
                    System.out.print(", ");
                }
                System.out.print(current.task.id);
                first = false;
                current = current.next;
            }
            System.out.println();
        }
    }

    // 4. Custom Insertion Sort (Displays pending tasks without altering internal heap structure)
    public static void sortPendingTasks(Task[] tasks) {
        int n = tasks.length;
        for (int i = 1; i < n; i++) {
            Task key = tasks[i];
            int j = i - 1;

            while (j >= 0 && shouldComeBefore(key, tasks[j])) {
                tasks[j + 1] = tasks[j];
                j--;
            }
            tasks[j + 1] = key;
        }
    }

    private static boolean shouldComeBefore(Task a, Task b) {
        if (a.priority != b.priority) {
            return a.priority < b.priority;
        }
        return a.sequence < b.sequence;
    }

    // 5. Console Interface Driver
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        MinHeap pendingHeap = new MinHeap(10);
        CustomQueue completedQueue = new CustomQueue();

        int taskCounter = 1;
        int sequenceCounter = 1;

        while (true) {
            System.out.println("\n--- TASK SCHEDULER SYSTEM ---");
            System.out.println("1. Add Task");
            System.out.println("2. Execute Next Task");
            System.out.println("3. View Pending Tasks (Sorted by Priority)");
            System.out.println("4. View Completed Tasks");
            System.out.println("5. Exit");
            System.out.print("Select Option: ");

            if (!scanner.hasNextLine()) {
                break;
            }

            String input = scanner.nextLine().trim();
            int choice;

            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid selection. Enter a number between 1 and 5.");
                continue;
            }

            switch (choice) {
                case 1:
                    System.out.print("Enter Task Name: ");
                    String name = scanner.nextLine().trim();

                    System.out.print("Enter Priority (1-5): ");
                    String priorityInput = scanner.nextLine().trim();
                    int priority;
                    try {
                        priority = Integer.parseInt(priorityInput);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid priority. Enter a value from 1 to 5.");
                        break;
                    }

                    if (priority < 1 || priority > 5) {
                        System.out.println("Invalid priority. Enter a value from 1 to 5.");
                        break;
                    }

                    String taskId = "T" + taskCounter++;
                    Task newTask = new Task(taskId, name, priority, sequenceCounter++);
                    pendingHeap.insert(newTask);

                    System.out.println("Task added. ID: " + newTask.id + " | " + newTask.name + " | P" + newTask.priority);
                    break;

                case 2:
                    if (pendingHeap.isEmpty()) {
                        System.out.println("Pending tasks: 0");
                    } else {
                        Task executed = pendingHeap.extractMin();
                        completedQueue.enqueue(executed);
                        System.out.println("Executing " + executed.id + ": " + executed.name + " (P" + executed.priority + ")");
                    }
                    break;

                case 3:
                    if (pendingHeap.isEmpty()) {
                        System.out.println("Pending tasks: 0");
                    } else {
                        Task[] pendingCopy = pendingHeap.getCopy();
                        sortPendingTasks(pendingCopy);
                        System.out.print("Pending tasks: ");
                        for (int i = 0; i < pendingCopy.length; i++) {
                            System.out.print(pendingCopy[i].id + " (P" + pendingCopy[i].priority + ")");
                            if (i < pendingCopy.length - 1) {
                                System.out.print(", ");
                            }
                        }
                        System.out.println();
                    }
                    break;

                case 4:
                    completedQueue.displayCompleted();
                    break;

                case 5:
                    System.out.println("Exiting system.");
                    return;

                default:
                    System.out.println("Invalid option. Enter a value from 1 to 5.");
            }
        }
    }
}