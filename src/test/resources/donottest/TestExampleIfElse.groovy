class TestExampleIfElse {
    public static void main(String[] args) {
       println add(0, 10)
       println add(1, 2)
    }

    public static Integer add(Integer x, Integer y) {
        if(x == 0 ) {
            return y
        } else {
            return x + y
        }
    }
}
