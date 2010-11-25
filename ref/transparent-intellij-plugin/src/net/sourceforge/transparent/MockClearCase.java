package net.sourceforge.transparent;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;
import java.util.Arrays;

public class MockClearCase extends Assert implements ClearCase {
    private Set _elements = new HashSet();
    private Set _checkedOutElements = new HashSet();
    private String[] _steps;
    private int _nextStepIndex = -1;

   public MockClearCase() {
      this(new File[] {new File("C:\\dev"), new File("C:\\cc_views")});
   }

   public MockClearCase(File[] elements) {
      _elements.addAll(Arrays.asList(elements));
   }

    public String getName() {
        return MockClearCase.class.getName();
    }

    public void assertStep(String step) {
        assertSteps(new String[]{step});
    }

    public void assertSteps(String[] steps) {
        _steps = steps;
        _nextStepIndex = 0;
    }

    public void verifySteps() {
        if (_steps.length > _nextStepIndex)
            fail(_steps[_nextStepIndex] + " not executed");
        _steps = null;
        _nextStepIndex = 0;
    }

    protected void log(String message) {
        if (_steps != null) {
            if (_nextStepIndex < _steps.length)
                assertEquals("step " + _nextStepIndex, _steps[_nextStepIndex++], message);
            else
                fail("unexpected step: " + message);
        } else {
            if (_nextStepIndex == -1)
                System.out.println(message);
        } 
    }

    public void move(File file, File target, String comment) {
        assertTrue("file status is not NOT_AN_ELEMENT", Status.NOT_AN_ELEMENT != getStatus(file));
        assertStatusEquals(Status.CHECKED_OUT, file.getParentFile());
        assertStatusEquals(Status.CHECKED_OUT, target.getParentFile());
        assertStatusEquals(Status.NOT_AN_ELEMENT, target);
        _elements.remove(file);
        _elements.add(target);
        if (_checkedOutElements.contains(file)) {
            _checkedOutElements.remove(file);
            _checkedOutElements.add(target);
        }
        file.renameTo(target);

        log("moving " + file.getName() + " to " + target.getName());
    }

    public void undoCheckOut(File file) {
        assertStatusEquals(Status.CHECKED_OUT, file);
        _checkedOutElements.remove(file);

        log("uncheckout " + file.getName());
    }

    public void checkIn(File file, String comment) {
        assertStatusEquals(Status.CHECKED_OUT, file);
        _checkedOutElements.remove(file);

        log("checkin " + file.getName());
    }

    public void checkOut(File file, boolean isReserved) {
        assertStatusEquals(Status.CHECKED_IN, file);
        _checkedOutElements.add(file);

        log("checkout " + (isReserved ? "reserved" : "unreserved") + " " + file.getName());
    }

    public void delete(File file, String comment) {
        assertStatusEquals(Status.CHECKED_IN, file);
        assertStatusEquals(Status.CHECKED_OUT, file.getParentFile());
        file.delete();
        assertTrue(_elements.remove(file));

        log("delete " + file.getName());
    }

    public void add(File file, String comment) {
        assertStatusEquals(Status.NOT_AN_ELEMENT, file);
        assertStatusEquals(Status.CHECKED_OUT, file.getParentFile());
        _elements.add(file);
        _checkedOutElements.add(file);

        log("add " + (file.isDirectory()?"directory ":"file ") + file.getName());
    }

    public Status getStatus(File file) {
        if (!isElement(file)) {
            return Status.NOT_AN_ELEMENT;
        } else if (isCheckedOut(file)) {
            return Status.CHECKED_OUT;
        } else {
            return Status.CHECKED_IN;
        }
    }

    public boolean isElement(File file) {
        return _elements.contains(file);
    }

    public boolean isCheckedOut(File file) {
        return _checkedOutElements.contains(file);
    }

    public void cleartool(String cmd) {
    }

    public CheckedOutStatus getCheckedOutStatus(File file) {
        return null;
    }

    public Set getElements() {
        return _elements;
    }

    public void assertStatusEquals(Status status, File file) {
        try {
            assertEquals("status of " + file.getPath(), status, getStatus(file));
        } catch (AssertionFailedError e) {
            dumpState();
            throw e;
        }
    }

    private void dumpState() {
        System.out.println("Elements={");
        for (Iterator iterator = _elements.iterator(); iterator.hasNext();) {
            File file = (File) iterator.next();
            if (_checkedOutElements.contains(file))
                System.out.print("  (co)");
            else
                System.out.print("      ");
            System.out.println(file.getPath());
        }
        System.out.println("}");
    }

}
