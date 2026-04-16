import { useState } from 'react';
import { ChevronDown, ChevronUp } from 'lucide-react';

interface FilterSectionProps {
  title: string;
  children: React.ReactNode;
  defaultOpen?: boolean;
}

export function FilterSection({ title, children, defaultOpen = true }: FilterSectionProps) {
  const [isOpen, setIsOpen] = useState(defaultOpen);

  return (
    <div className="border-b border-amber-200 pb-4 mb-4">
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="flex items-center justify-between w-full text-left mb-3"
      >
        <h3 className="font-semibold text-gray-900">{title}</h3>
        {isOpen ? (
          <ChevronUp className="w-4 h-4 text-gray-500" />
        ) : (
          <ChevronDown className="w-4 h-4 text-gray-500" />
        )}
      </button>
      {isOpen && <div className="space-y-2">{children}</div>}
    </div>
  );
}

interface CheckboxFilterProps {
  label: string;
  count?: number;
  checked?: boolean;
  onChange?: (checked: boolean) => void;
}

export function CheckboxFilter({ label, count, checked = false, onChange }: CheckboxFilterProps) {
  return (
    <label className="flex items-center space-x-2 cursor-pointer hover:text-red-600 transition-colors">
      <input
        type="checkbox"
        checked={checked}
        onChange={(e) => onChange?.(e.target.checked)}
        className="w-4 h-4 rounded border-gray-300 text-red-600 focus:ring-red-500"
      />
      <span className="text-sm text-gray-700">{label}</span>
      {count !== undefined && (
        <span className="text-xs text-gray-400 ml-auto">({count})</span>
      )}
    </label>
  );
}